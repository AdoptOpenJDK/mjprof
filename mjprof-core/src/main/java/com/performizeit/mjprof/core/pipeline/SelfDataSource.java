package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.model.Profile;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import com.performizeit.mjprof.core.plugin.types.DataSource;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Snapshots the threads of the JVM this code is running in, straight off {@link ThreadMXBean} — no
 * JMX connection, no network, no remote process.
 *
 * <p>Deliberately NOT annotated with @Plugin: an in-process source cannot mean anything on a command
 * line, and it must not appear in supported_monads.txt. Reach it through Pipeline.fromSelf().
 *
 * <p>Produces name, tid, state, stack and daemon (which {@code JmxDataSourcePlugin} never sets),
 * plus, when CPU collection is on, cpu_ns and wall_ms with the same meaning
 * {@code JmxDataSourcePluginWithCpu} gives them: cpu_ns is the CPU burned <b>during the sample's
 * measurement interval</b>, not since the thread started, and wall_ms is that interval. It does not
 * set %cpu; merging samples recomputes it from cpu_ns and wall_ms. Downstream stages therefore work
 * unchanged against either source — notably merge(Attr.TID), which sums cpu_ns.
 *
 * <p>With CPU collection on, each sample <i>is</i> the interval: the source reads CPU, sleeps
 * {@code sleepMs}, reads CPU again, so a run of {@code count} samples blocks for roughly
 * {@code count * sleepMs} (there is no separate sleep between samples). {@code sleepMs == 0} leaves
 * an interval of essentially zero length, so cpu_ns is present but near zero and useless for
 * ranking — pass a real sleepMs when you want a CPU profile.
 */
final class SelfDataSource implements DataSource, GeneratorHandler<ThreadDump> {

  /** Matches the ^\d\d\d\d-\d\d-\d\d prefix the stream parser splits concatenated dumps on. */
  private static final DateTimeFormatter HEADER_STAMP =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final int count;
  private final int sleepMs;
  private final boolean collectCpu;
  private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

  private int produced = 0;

  /** Rejects sampling parameters before anything is constructed; {@code caller} names the API method. */
  static void validateSampling(String caller, int count, int sleepMs) {
    if (count < 1) {
      throw new MjprofException(caller + ": count must be at least 1, got " + count);
    }
    if (sleepMs < 0) {
      throw new MjprofException(caller + ": sleepMs must not be negative, got " + sleepMs);
    }
  }

  SelfDataSource(int count, int sleepMs, boolean collectCpu) {
    validateSampling("fromSelf", count, sleepMs);
    this.count = count;
    this.sleepMs = sleepMs;
    this.collectCpu = collectCpu;
  }

  @Override
  public ThreadDump generate() {
    ThreadDump dump = new ThreadDump();
    dump.setHeader(LocalDateTime.now().format(HEADER_STAMP)
        + "\nFull thread dump of this process (pid " + ProcessHandle.current().pid() + ")");

    boolean withCpu = collectCpu
        && threadMXBean.isThreadCpuTimeSupported()
        && threadMXBean.isThreadCpuTimeEnabled();

    // lock info is not mapped into the model, so don't pay to collect it
    java.lang.management.ThreadInfo[] threads = threadMXBean.dumpAllThreads(false, false);

    // Same shape as JmxDataSourcePluginWithCpu: bracket an interval with two CPU readings and
    // report the difference, so cpu_ns is per-interval and merging samples sums to the window.
    long[] cpuPre = null;
    long[] cpuPost = null;
    long wallMs = 0;
    if (withCpu) {
      long pre = System.currentTimeMillis();
      cpuPre = readCpu(threads);
      sleep();
      cpuPost = readCpu(threads);
      wallMs = System.currentTimeMillis() - pre;
    } else if (collectCpu) {
      sleep();   // CPU time unavailable on this JVM: still honour the requested sampling interval
    }

    for (int i = 0; i < threads.length; i++) {
      if (threads[i] == null) {
        continue;   // thread terminated between enumeration and dump
      }
      dump.addThreadInfo(withCpu
          ? convert(threads[i], cpuPre[i], cpuPost[i], wallMs)
          : convert(threads[i], -1, -1, 0));
    }
    produced++;
    return dump;
  }

  /** CPU per thread, in the array positions of {@code threads}; -1 where it is not measurable. */
  private long[] readCpu(java.lang.management.ThreadInfo[] threads) {
    long[] cpu = new long[threads.length];
    for (int i = 0; i < threads.length; i++) {
      cpu[i] = threads[i] == null ? -1 : threadMXBean.getThreadCpuTime(threads[i].getThreadId());
    }
    return cpu;
  }

  private ThreadInfo convert(java.lang.management.ThreadInfo live, long cpuPre, long cpuPost,
                             long wallMs) {
    HashMap<String, Object> props = new HashMap<>();
    props.put(ThreadInfoProps.NAME, live.getThreadName());
    props.put(ThreadInfoProps.TID, live.getThreadId());
    props.put(ThreadInfoProps.STATE, live.getThreadState().name());
    props.put(ThreadInfoProps.DAEMON, live.isDaemon());

    Profile profile = new Profile();
    profile.addSingle(live.getStackTrace());
    props.put(ThreadInfoProps.STACK, profile);

    // -1 on either reading means the thread was gone or unmeasurable; a thread that only appeared
    // for the second reading would give a bogus delta, so both must be real and ordered.
    if (cpuPre >= 0 && cpuPost >= cpuPre) {
      props.put(ThreadInfoProps.CPUNS, cpuPost - cpuPre);
      props.put(ThreadInfoProps.WALL, wallMs);
    }
    return new ThreadInfo(props);
  }

  @Override
  public boolean isDone() {
    return produced >= count;
  }

  @Override
  public void sleepBetweenIteration() {
    if (collectCpu) {
      return;   // the sleep is the measurement interval, already taken inside generate()
    }
    sleep();
  }

  private void sleep() {
    if (sleepMs <= 0) {
      return;
    }
    try {
      Thread.sleep(sleepMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MjprofException("interrupted while sampling this process", e);
    }
  }
}
