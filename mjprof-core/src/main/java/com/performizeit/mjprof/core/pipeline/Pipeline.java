package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.api.ThreadInfoComparator;
import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.model.Profile;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import com.performizeit.mjprof.core.plugin.types.DataSource;
import com.performizeit.mjprof.core.plugin.types.DumpReducer;
import com.performizeit.mjprof.core.plugin.types.Filter;
import com.performizeit.mjprof.core.plugin.types.SingleThreadMapper;
import com.performizeit.mjprof.core.plugins.combiners.AccumulateDumps;
import com.performizeit.mjprof.core.plugins.comparators.PropComparator;
import com.performizeit.mjprof.core.plugins.comparators.ReversePropComparator;
import com.performizeit.mjprof.core.plugins.dataSource.JmxDataSourcePlugin;
import com.performizeit.mjprof.core.plugins.dataSource.JmxDataSourcePluginWithCpu;
import com.performizeit.mjprof.core.plugins.dataSource.PathDataSourcePlugin;
import com.performizeit.mjprof.core.plugins.dataSource.StdinDataSourcePlugin;
import com.performizeit.mjprof.core.plugins.filters.JStackFilterFieldContains;
import com.performizeit.mjprof.core.plugins.filters.JStackFilterFieldNotContains;
import com.performizeit.mjprof.core.plugins.filters.JStackFilterFieldRegexMatches;
import com.performizeit.mjprof.core.plugins.mappers.GroupByProp;
import com.performizeit.mjprof.core.plugins.mappers.MergedCallees;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.PropEliminator;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.RemoveThreadNameNumericalSuffix;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.StackBottom;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.StackFrameContains;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.StackFrameNotContains;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.StackTop;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.TrimBelow;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.stackframe.AtEliminator;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.stackframe.FileNameEliminator;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.stackframe.PackageEliminator;
import com.performizeit.mjprof.core.plugins.mergers.StackTreeMerger;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;
import com.performizeit.mjprof.core.plumbing.PipeHandler;

import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * Typed, thread-free thread-dump analysis pipeline.
 *
 * <p>Stages are plugin instances: the plugin constructor arguments are the typed stage arguments, so
 * the compiler checks them. Every method returns a new Pipeline, so a Pipeline can be shared and
 * collected from more than once. That reuse guarantee holds for sources that reconstruct their data
 * on every run — {@link #fromFile(String)} and {@link #fromStdin()} re-parse the text, {@link
 * #fromJmx} and {@link #fromJmxWithCpu} re-sample the target — so a fresh Pipeline call gets fresh
 * {@link ThreadDump}/{@link ThreadInfo}/{@link Profile} objects every time. It does NOT hold for
 * {@link #fromDumps(Collection)}: that source hands out the very same objects the caller passed in,
 * on every run, and several stages mutate those objects in place rather than copying them. See {@link
 * #fromDumps(Collection)}'s own Javadoc.
 *
 * <p>Collection happens on the calling thread. A source configured to take several samples blocks
 * for its own sleep interval: fromJmx(hostPort, 3, 1000, ...) blocks roughly two seconds.
 *
 * <p>Stages are held as suppliers, not instances, so every {@link #collect()} builds a fresh stage
 * object for every internal convenience method (merge, group, sort, contains, tree, ...) — a
 * stateful stage such as the one behind {@link #merge(Attr)} does not carry state between runs.
 * Building a fresh stage instance does not, by itself, protect the {@link ThreadDump}s flowing
 * through it from being mutated — see the {@link #fromDumps(Collection)} note above.
 *
 * <p>Known limitation, deliberate: {@link #from(Object) from(S source)} reuses the caller's plugin
 * instance rather than building it fresh, so a Pipeline built that way is single-use. Likewise the
 * four {@code map(P stage)} overloads are handed an already-constructed instance with no way to
 * fabricate a fresh copy of it, so that one stage is shared across repeated {@link #collect()} calls
 * on the same or derived Pipelines — construct a new plugin instance per run if that matters.
 *
 * <p>To sample on your own schedule and analyse later, keep the ThreadDump objects and use
 * fromDumps. Writing dumps out as jstack text and re-reading them preserves count, cpu_ns, wall_ms
 * and %cpu, but dumps taken over JMX do not re-split when concatenated, because their header is not
 * date-prefixed — so several JMX samples in one file come back as a single dump.
 */
public final class Pipeline {

  private final Supplier<GeneratorHandler<ThreadDump>> source;
  private final List<Supplier<PipeHandler<ThreadDump, ThreadDump>>> stages;

  private Pipeline(Supplier<GeneratorHandler<ThreadDump>> source,
                   List<Supplier<PipeHandler<ThreadDump, ThreadDump>>> stages) {
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

  /** Takes one snapshot of the threads of the JVM this code is running in. */
  public static Pipeline fromSelf() {
    return fromSelf(1, 0);
  }

  /**
   * Samples the threads of the JVM this code is running in.
   *
   * @param count   number of snapshots to take
   * @param sleepMs milliseconds to wait between snapshots; blocks the calling thread
   */
  public static Pipeline fromSelf(int count, int sleepMs) {
    return selfPipeline(count, sleepMs, false);
  }

  /**
   * As {@link #fromSelf(int, int)}, additionally recording per-thread CPU time, so several samples
   * merged on {@code Attr.TID} give a sampled CPU profile of this process.
   */
  public static Pipeline fromSelfWithCpu(int count, int sleepMs) {
    return selfPipeline(count, sleepMs, true);
  }

  private static Pipeline selfPipeline(int count, int sleepMs, boolean collectCpu) {
    new SelfDataSource(count, sleepMs, collectCpu);   // eager parameter validation
    return new Pipeline(() -> new SelfDataSource(count, sleepMs, collectCpu), List.of());
  }

  /**
   * Analyses thread dumps the caller collected itself — sample on your own schedule, keep the dumps,
   * then merge and analyse them later. Combine with merge(Attr.TID) to turn N samples into one
   * sampled profile.
   *
   * <p><b>Mutation hazard:</b> unlike the other sources, this one hands every run the same {@link
   * ThreadDump}/{@link ThreadInfo}/{@link Profile} instances the caller passed in — there is no
   * re-parse or re-fetch to produce fresh copies. Several stages mutate those instances in place
   * instead of copying them, notably {@link #stackTop(int)}, {@link #trimBelow(String)} and the
   * other stack-shaping stages (they prune the {@link Profile} tree destructively), {@link
   * #mergedCallees(String)} (calls {@code filterDown} on the incoming {@link Profile}), and the
   * frame eliminators such as {@link #withoutAt()} (rewrite frame strings in place). Running a
   * Pipeline built this way through any such stage — via {@link #collect()}, {@link #collectOne()},
   * {@link #threads()}, {@link #tree()} or {@link #treeText()} — permanently changes the dumps you
   * passed in. So do not rely on the dumps you gave to {@code fromDumps} still being unmodified after
   * a run, and do not assume a second {@link #collect()} on the resulting Pipeline sees the original
   * data rather than what the first run left behind. If you need the originals intact, keep a deep
   * copy before collecting, or re-collect from a source that reconstructs its data per run instead.
   */
  public static Pipeline fromDumps(Collection<ThreadDump> dumps) {
    if (dumps == null) {
      throw new MjprofException("fromDumps(dumps): dumps must not be null");
    }
    int i = 0;
    for (ThreadDump d : dumps) {
      if (d == null) {
        throw new MjprofException("fromDumps(dumps): dumps must not contain a null element (index "
            + i + ") - a null dump would silently truncate the source");
      }
      i++;
    }
    List<ThreadDump> snapshot = List.copyOf(dumps);
    return new Pipeline(() -> new DumpsDataSource(snapshot), List.of());
  }

  /**
   * Samples thread dumps over JMX.
   *
   * @param hostPort host:port of the target JMX endpoint; validated eagerly, so a malformed value
   *                 throws {@link MjprofException} here rather than at {@link #collect()}
   * @param count    number of dumps to take
   * @param sleepMs  milliseconds to wait between dumps; blocks the calling thread
   * @param user     JMX username, or null for no credentials
   * @param pass     JMX password, or null for no credentials
   */
  public static Pipeline fromJmx(String hostPort, int count, int sleepMs, String user, String pass) {
    validateJmxHostPort(hostPort);
    String safeUser = user == null ? "" : user;
    String safePass = pass == null ? "" : pass;
    return new Pipeline(() -> new JmxDataSourcePlugin(hostPort, count, sleepMs, safeUser, safePass), List.of());
  }

  /** As fromJmx, additionally collecting per-thread CPU time. */
  public static Pipeline fromJmxWithCpu(String hostPort, int count, int sleepMs, String user, String pass) {
    validateJmxHostPort(hostPort);
    String safeUser = user == null ? "" : user;
    String safePass = pass == null ? "" : pass;
    return new Pipeline(() -> new JmxDataSourcePluginWithCpu(hostPort, count, sleepMs, safeUser, safePass), List.of());
  }

  /**
   * Rejects a null or malformed {@code host:port} before any plugin is constructed.
   *
   * <p>{@code JmxDataSourcePlugin}'s constructor calls {@code System.exit(1)} when
   * {@code JMXConnection} throws {@link MalformedURLException} — reasonable for the CLI, fatal for
   * a library caller. So a bad host:port must never reach that constructor. The URL template below
   * is deliberately copied verbatim from {@code JMXConnection}, so acceptance here is identical to
   * what the plugin itself would accept.
   */
  private static void validateJmxHostPort(String hostPort) {
    if (hostPort == null) {
      throw new MjprofException("JMX host:port must not be null");
    }
    try {
      new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostPort + "/jmxrmi");
    } catch (MalformedURLException e) {
      throw new MjprofException("malformed JMX host:port: " + hostPort, e);
    }
  }

  // ------------------------------------------------------------ generic stage

  /** Adds any single-thread mapper plugin instance as a stage. */
  public <P extends SingleThreadMapper & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return addInstance(stage);
  }

  /** Adds any dump reducer plugin instance as a stage. */
  public <P extends DumpReducer & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return addInstance(stage);
  }

  /** Adds any filter plugin instance as a stage. */
  public <P extends Filter & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return addInstance(stage);
  }

  /** Adds any comparator plugin instance as a stage. */
  public <P extends ThreadInfoComparator & PipeHandler<ThreadDump, ThreadDump>> Pipeline map(P stage) {
    return addInstance(stage);
  }

  /**
   * Adds an already-constructed stage instance, reused unchanged on every {@link #collect()} — see
   * the class-level known limitation.
   */
  private Pipeline addInstance(PipeHandler<ThreadDump, ThreadDump> stage) {
    if (stage == null) {
      throw new MjprofException("stage must not be null");
    }
    return add(() -> stage);
  }

  /**
   * Groups threads within each dump on a composite key of the given attributes, collapsing matching
   * threads into one entry carrying count, summed cpu_ns and a merged stack tree. With no arguments,
   * merges every thread of the dump into one entry.
   *
   * @throws MjprofException if given Attr.STACK — stack profiles have no value equality, so grouping
   *                         on them collapses nothing. Use merge(Attr.TID) followed by tree().
   */
  public Pipeline group(Attr... attrs) {
    requireNonNull(attrs, "group(attrs)", "attrs");
    for (int i = 0; i < attrs.length; i++) {
      requireNonNull(attrs[i], "group(attrs)", "attrs[" + i + "]");
      rejectStack(attrs[i], "group");
    }
    if (attrs.length == 0) {
      return add(() -> new GroupByProp(""));
    }
    List<String> props = new ArrayList<>();
    for (Attr a : attrs) {
      props.add(a.getAttrName());
    }
    return add(() -> new GroupByProp(props));
  }

  /** Combines every dump into one, matching threads on their thread id. */
  public Pipeline merge() {
    return merge(Attr.TID);
  }

  /**
   * Combines every dump into one, matching threads on the given attribute. Summed cpu_ns, max
   * wall_ms, recomputed %cpu and merged stack trees make N samples into one sampled profile.
   *
   * @throws MjprofException if given Attr.STACK — see group(Attr...).
   */
  public Pipeline merge(Attr attr) {
    requireNonNull(attr, "merge(attr)", "attr");
    rejectStack(attr, "merge");
    return add(() -> new AccumulateDumps(attr.getAttrName()));
  }

  private static void rejectStack(Attr attr, String method) {
    if (Attr.STACK.getAttrName().equals(attr.getAttrName())) {
      throw new MjprofException(method + "(Attr.STACK) is not supported: stack profiles have no "
          + "value equality, so nothing would be collapsed. Use merge(Attr.TID) then tree().");
    }
  }

  /** Rejects a null argument eagerly, at the call to the named public method, not at collect() time. */
  private static void requireNonNull(Object value, String signature, String param) {
    if (value == null) {
      throw new MjprofException(signature + ": " + param + " must not be null");
    }
  }

  // ------------------------------------------------- filters

  /** Keeps threads whose attribute contains the given substring. */
  public Pipeline contains(Attr attr, String value) {
    requireNonNull(attr, "contains(attr, value)", "attr");
    requireNonNull(value, "contains(attr, value)", "value");
    return add(() -> new JStackFilterFieldContains(attr.getAttrName(), value));
  }

  /** Keeps threads whose attribute does not contain the given substring. */
  public Pipeline notContains(Attr attr, String value) {
    requireNonNull(attr, "notContains(attr, value)", "attr");
    requireNonNull(value, "notContains(attr, value)", "value");
    return add(() -> new JStackFilterFieldNotContains(attr.getAttrName(), value));
  }

  /** Keeps threads whose attribute matches the given regular expression. */
  public Pipeline matches(Attr attr, String regex) {
    requireNonNull(attr, "matches(attr, regex)", "attr");
    requireNonNull(regex, "matches(attr, regex)", "regex");
    return add(() -> new JStackFilterFieldRegexMatches(attr.getAttrName(), regex));
  }

  // ------------------------------------------------- stack shaping

  /** Keeps only the top n stack frames of each thread. */
  public Pipeline stackTop(int frames) {
    return add(() -> new StackTop(frames));
  }

  /** Keeps only the bottom n stack frames of each thread. */
  public Pipeline stackBottom(int frames) {
    return add(() -> new StackBottom(frames));
  }

  /** Drops stack frames below the first frame containing the given text. */
  public Pipeline trimBelow(String frameText) {
    requireNonNull(frameText, "trimBelow(frameText)", "frameText");
    return add(() -> new TrimBelow(frameText));
  }

  /** Keeps only stack frames containing the given text. */
  public Pipeline frameContains(String text) {
    requireNonNull(text, "frameContains(text)", "text");
    return add(() -> new StackFrameContains(text));
  }

  /** Drops stack frames containing the given text. */
  public Pipeline frameNotContains(String text) {
    requireNonNull(text, "frameNotContains(text)", "text");
    return add(() -> new StackFrameNotContains(text));
  }

  // ------------------------------------------------- ordering

  /** Sorts threads by an attribute, ascending. */
  public Pipeline sort(Attr attr) {
    requireNonNull(attr, "sort(attr)", "attr");
    return add(() -> new PropComparator(attr.getAttrName()));
  }

  /** Sorts threads by an attribute, descending. */
  public Pipeline sortDesc(Attr attr) {
    requireNonNull(attr, "sortDesc(attr)", "attr");
    return add(() -> new ReversePropComparator(attr.getAttrName()));
  }

  // ------------------------------------------------- stack trees

  /**
   * Merges all thread stacks of every dump into one combined call tree, collapsing the whole
   * pipeline down to a single {@link ThreadInfo} whose {@code stack} is that merged {@link Profile}.
   *
   * <p>{@link StackTreeMerger} is a {@code Terminal} plugin — it natively produces a {@code String},
   * not a {@code ThreadDump}, since in the CLI it is always the last step before output. To fit the
   * typed pipeline (every stage is {@code ThreadDump -> ThreadDump}), this wraps it unmodified: each
   * dump is accumulated via its public {@code addStackDump}, and once the source is exhausted the
   * merged text is parsed back into a {@link Profile} via its public string constructor.
   *
   * <p>That round-trip through text is fallible in one narrow case: {@link Profile}'s string
   * constructor guesses the single- vs multi-thread format by sniffing for the substring {@code "]\
   * "}, which requires some top-level entry in the rendered tree to have exactly one child. A merged
   * tree where every top-level entry is a leaf (e.g. two threads with one distinct frame each) or
   * branches into 2 or more children never produces that substring, so it is silently mis-parsed as
   * a single flat stack instead of a multi-thread tree. {@link #treeText()} does not round-trip
   * through text and is immune to this.
   */
  public Pipeline tree() {
    return add(() -> new TreeMergeStage());
  }

  private static final class TreeMergeStage implements PipeHandler<ThreadDump, ThreadDump> {
    private final StackTreeMerger merger = new StackTreeMerger();

    @Override
    public ThreadDump handleMsg(ThreadDump msg) {
      merger.addStackDump(msg);
      return null;
    }

    @Override
    public ThreadDump handleDone() {
      ThreadInfo merged = new ThreadInfo();
      merged.setVal(ThreadInfoProps.STACK, new Profile(merger.toString()));
      return new ThreadDump(new JStackHeader(""), List.of(merged));
    }
  }

  /** Merges the callees of the given method across all threads. */
  public Pipeline mergedCallees(String methodName) {
    requireNonNull(methodName, "mergedCallees(methodName)", "methodName");
    return add(() -> new MergedCallees(methodName));
  }

  // ------------------------------------------------- elimination

  /** Removes an attribute from every thread. */
  public Pipeline withoutProp(Attr attr) {
    requireNonNull(attr, "withoutProp(attr)", "attr");
    return add(() -> new PropEliminator(attr.getAttrName()));
  }

  /** Strips trailing numbers from thread names so pool threads group together. */
  public Pipeline withoutNameSuffix() {
    return add(() -> new RemoveThreadNameNumericalSuffix());
  }

  /** Removes package names from stack frames. */
  public Pipeline withoutPackages() {
    return add(() -> new PackageEliminator());
  }

  /** Removes file names and line numbers from stack frames. */
  public Pipeline withoutFileNames() {
    return add(() -> new FileNameEliminator());
  }

  /** Removes the leading "at " from stack frames. */
  public Pipeline withoutAt() {
    return add(() -> new AtEliminator());
  }

  Pipeline add(Supplier<PipeHandler<ThreadDump, ThreadDump>> stageFactory) {
    if (stageFactory == null) {
      throw new MjprofException("stage must not be null");
    }
    List<Supplier<PipeHandler<ThreadDump, ThreadDump>>> next = new ArrayList<>(stages);
    next.add(stageFactory);
    return new Pipeline(source, next);
  }

  // ------------------------------------------------------------- terminals

  /** Runs the pipeline and returns one dump per dump the source produced. */
  public List<ThreadDump> collect() {
    return PipelineRunner.run(buildSource(), freshStages());
  }

  /** Runs the pipeline and returns its single dump, failing if the source produced any other number. */
  public ThreadDump collectOne() {
    GeneratorHandler<ThreadDump> generator = buildSource();
    List<ThreadDump> all = PipelineRunner.run(generator, freshStages());
    if (all.size() != 1) {
      throw new MjprofException("expected exactly one thread dump from "
          + generator.getClass().getName() + ", got " + all.size());
    }
    return all.get(0);
  }

  private GeneratorHandler<ThreadDump> buildSource() {
    try {
      return source.get();
    } catch (RuntimeException e) {
      throw new MjprofException("failed to build data source: " + e.getMessage(), e);
    }
  }

  /** Runs the pipeline and returns every thread of every resulting dump. */
  public List<ThreadInfo> threads() {
    List<ThreadInfo> out = new ArrayList<>();
    for (ThreadDump d : collect()) {
      out.addAll(d.getThreadInfos());
    }
    return out;
  }

  /**
   * Runs the pipeline and returns the merged call tree of every dump it produced, rendered exactly
   * as the CLI's {@code tree} monad would print it — the honest, allocation- and round-trip-free
   * path: no synthetic {@link ThreadDump}/{@link ThreadInfo} is built and no {@link Profile} is
   * reconstructed from text. Prefer {@link #tree()} instead when further stages need to chain after
   * the merge; this method is the leaf.
   */
  public String treeText() {
    StackTreeMerger merger = new StackTreeMerger();
    for (ThreadDump d : collect()) {
      merger.addStackDump(d);
    }
    return merger.toString();
  }

  private List<PipeHandler<ThreadDump, ThreadDump>> freshStages() {
    List<PipeHandler<ThreadDump, ThreadDump>> out = new ArrayList<>(stages.size());
    for (Supplier<PipeHandler<ThreadDump, ThreadDump>> factory : stages) {
      out.add(factory.get());
    }
    return out;
  }
}
