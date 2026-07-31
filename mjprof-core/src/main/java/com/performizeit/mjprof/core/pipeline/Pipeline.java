package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.ThreadInfoComparator;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.plugin.types.DataSource;
import com.performizeit.mjprof.core.plugin.types.DumpReducer;
import com.performizeit.mjprof.core.plugin.types.Filter;
import com.performizeit.mjprof.core.plugin.types.SingleThreadMapper;
import com.performizeit.mjprof.core.plugins.dataSource.JmxDataSourcePlugin;
import com.performizeit.mjprof.core.plugins.dataSource.JmxDataSourcePluginWithCpu;
import com.performizeit.mjprof.core.plugins.dataSource.PathDataSourcePlugin;
import com.performizeit.mjprof.core.plugins.dataSource.StdinDataSourcePlugin;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;
import com.performizeit.mjprof.core.plumbing.PipeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Typed, thread-free thread-dump analysis pipeline.
 *
 * <p>Stages are plugin instances: the plugin constructor arguments are the typed stage arguments, so
 * the compiler checks them. Every method returns a new Pipeline, so a Pipeline can be shared and
 * collected from more than once — the data source is constructed fresh per run.
 *
 * <p>Collection happens on the calling thread. A source configured to take several samples blocks
 * for its own sleep interval: fromJmx(hostPort, 3, 1000, ...) blocks roughly two seconds.
 *
 * <p>Known limitations, deliberate: {@link #from(Object) from(S source)} reuses the caller's plugin
 * instance rather than building it fresh, so a Pipeline built that way is single-use, unlike the
 * {@code fromX} factories below. Likewise {@link #add} copies the stage <em>list</em>, not the stage
 * objects, on every call, so a stateful stage instance is shared across repeated {@link #collect()}
 * calls on the same or derived Pipelines.
 */
public final class Pipeline {

  private final Supplier<GeneratorHandler<ThreadDump>> source;
  private final List<PipeHandler<ThreadDump, ThreadDump>> stages;

  private Pipeline(Supplier<GeneratorHandler<ThreadDump>> source,
                   List<PipeHandler<ThreadDump, ThreadDump>> stages) {
    this.source = source;
    this.stages = stages;
  }

  // ---------------------------------------------------------------- sources

  /** Uses the given data source plugin instance. The instance is reused, so this Pipeline is single-use. */
  public static <S extends DataSource & GeneratorHandler<ThreadDump>> Pipeline from(S source) {
    if (source == null) {
      throw new MjprofException("data source must not be null");
    }
    return new Pipeline(() -> source, List.of());
  }

  /** Reads one or more thread dumps from a jstack-format text file. */
  public static Pipeline fromFile(String path) {
    if (path == null) {
      throw new MjprofException("path must not be null");
    }
    return new Pipeline(() -> new PathDataSourcePlugin(path), List.of());
  }

  /** Reads thread dumps from standard input. */
  public static Pipeline fromStdin() {
    return new Pipeline(StdinDataSourcePlugin::new, List.of());
  }

  /**
   * Samples thread dumps over JMX.
   *
   * @param hostPort host:port of the target JMX endpoint
   * @param count    number of dumps to take
   * @param sleepMs  milliseconds to wait between dumps; blocks the calling thread
   * @param user     JMX username, or null
   * @param pass     JMX password, or null
   */
  public static Pipeline fromJmx(String hostPort, int count, int sleepMs, String user, String pass) {
    return new Pipeline(() -> new JmxDataSourcePlugin(hostPort, count, sleepMs, user, pass), List.of());
  }

  /** As fromJmx, additionally collecting per-thread CPU time. */
  public static Pipeline fromJmxWithCpu(String hostPort, int count, int sleepMs, String user, String pass) {
    return new Pipeline(() -> new JmxDataSourcePluginWithCpu(hostPort, count, sleepMs, user, pass), List.of());
  }

  // ------------------------------------------------------------ generic stage

  /** Adds any single-thread mapper plugin instance as a stage. */
  public <P extends SingleThreadMapper & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  /** Adds any dump reducer plugin instance as a stage. */
  public <P extends DumpReducer & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  /** Adds any filter plugin instance as a stage. */
  public <P extends Filter & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  /** Adds any comparator plugin instance as a stage. */
  public <P extends ThreadInfoComparator & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return add(stage);
  }

  Pipeline add(PipeHandler<ThreadDump, ThreadDump> stage) {
    if (stage == null) {
      throw new MjprofException("stage must not be null");
    }
    List<PipeHandler<ThreadDump, ThreadDump>> next = new ArrayList<>(stages);
    next.add(stage);
    return new Pipeline(source, next);
  }

  // ------------------------------------------------------------- terminals

  /** Runs the pipeline and returns one dump per dump the source produced. */
  public List<ThreadDump> collect() {
    GeneratorHandler<ThreadDump> generator;
    try {
      generator = source.get();
    } catch (RuntimeException e) {
      throw new MjprofException("failed to build data source: " + e.getMessage(), e);
    }
    return PipelineRunner.run(generator, freshStages());
  }

  /** Runs the pipeline and returns its single dump, failing if the source produced any other number. */
  public ThreadDump collectOne() {
    List<ThreadDump> all = collect();
    if (all.size() != 1) {
      throw new MjprofException("expected exactly one thread dump, got " + all.size());
    }
    return all.get(0);
  }

  /** Runs the pipeline and returns every thread of every resulting dump. */
  public List<ThreadInfo> threads() {
    List<ThreadInfo> out = new ArrayList<>();
    for (ThreadDump d : collect()) {
      out.addAll(d.getThreadInfos());
    }
    return out;
  }

  private List<PipeHandler<ThreadDump, ThreadDump>> freshStages() {
    return new ArrayList<>(stages);
  }
}
