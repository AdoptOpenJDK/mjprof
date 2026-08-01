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
 * <p>Produces a superset of the remote JMX source's properties — name, tid, state, stack, and
 * optionally cpu_ns, plus daemon (which {@code JmxDataSourcePlugin} never sets) — so downstream
 * stages that only rely on the shared properties work unchanged against either source.
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

  SelfDataSource(int count, int sleepMs, boolean collectCpu) {
    if (count < 1) {
      throw new MjprofException("fromSelf: count must be at least 1, got " + count);
    }
    if (sleepMs < 0) {
      throw new MjprofException("fromSelf: sleepMs must not be negative, got " + sleepMs);
    }
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
    for (java.lang.management.ThreadInfo live : threadMXBean.dumpAllThreads(false, false)) {
      if (live == null) {
        continue;   // thread terminated between enumeration and dump
      }
      dump.addThreadInfo(convert(live, withCpu));
    }
    produced++;
    return dump;
  }

  private ThreadInfo convert(java.lang.management.ThreadInfo live, boolean withCpu) {
    HashMap<String, Object> props = new HashMap<>();
    props.put(ThreadInfoProps.NAME, live.getThreadName());
    props.put(ThreadInfoProps.TID, live.getThreadId());
    props.put(ThreadInfoProps.STATE, live.getThreadState().name());
    props.put(ThreadInfoProps.DAEMON, live.isDaemon());

    Profile profile = new Profile();
    profile.addSingle(live.getStackTrace());
    props.put(ThreadInfoProps.STACK, profile);

    if (withCpu) {
      long cpuNs = threadMXBean.getThreadCpuTime(live.getThreadId());
      if (cpuNs >= 0) {   // -1 means the thread is already gone or measurement is off
        props.put(ThreadInfoProps.CPUNS, cpuNs);
      }
    }
    return new ThreadInfo(props);
  }

  @Override
  public boolean isDone() {
    return produced >= count;
  }

  @Override
  public void sleepBetweenIteration() {
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
