package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.api.Plugin;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelfDataSourceTest {

  @Test
  void isNotRegisteredAsAMonad() {
    assertNull(SelfDataSource.class.getAnnotation(Plugin.class),
        "an in-process source is meaningless from the command line and must stay out of "
            + "supported_monads.txt");
  }

  @Test
  void snapshotContainsTheThreadRunningThisTest() {
    String thisThread = Thread.currentThread().getName();
    List<ThreadInfo> threads = Pipeline.fromSelf().threads();

    assertFalse(threads.isEmpty());
    assertTrue(threads.stream().anyMatch(t -> thisThread.equals(t.name())),
        "expected to find '" + thisThread + "' among " + threads.size() + " threads");
  }

  @Test
  void everyThreadHasANameAStateAndATid() {
    for (ThreadInfo t : Pipeline.fromSelf().threads()) {
      assertNotNull(t.name(), "name");
      assertNotNull(t.state(), "state of " + t.name());
      assertNotNull(t.tid(), "tid of " + t.name());
      // state must be a real Thread.State constant, not free text
      Thread.State.valueOf(t.state());
    }
  }

  @Test
  void theCallingThreadsStackIsCaptured() {
    String thisThread = Thread.currentThread().getName();
    ThreadInfo me = Pipeline.fromSelf()
        .contains(Attr.NAME, thisThread)
        .threads().stream().findFirst().orElseThrow();

    String stack = String.valueOf(me.stack());
    assertTrue(stack.contains("SelfDataSourceTest"),
        "the captured stack should mention this test class, was:\n" + stack);
  }

  @Test
  void countControlsHowManySnapshotsAreTaken() {
    assertEquals(3, Pipeline.fromSelf(3, 0).collect().size());
  }

  @Test
  void collectingTwiceFromTheSamePipelineEachYieldsTheFullSampleCount() {
    Pipeline pipeline = Pipeline.fromSelf(3, 0);

    assertEquals(3, pipeline.collect().size());
    assertEquals(3, pipeline.collect().size(),
        "a second collect() on the same Pipeline must build a fresh source, not reuse an "
            + "exhausted one");
  }

  @Test
  void mergingSelfSamplesFoldsEachThreadIntoOneEntry() {
    List<ThreadInfo> merged = Pipeline.fromSelf(3, 0).merge(Attr.TID).threads();
    List<ThreadInfo> single = Pipeline.fromSelf(1, 0).threads();

    // same thread appearing in 3 samples collapses to one entry, so merging 3 samples
    // must not produce roughly 3x the threads of a single sample
    assertTrue(merged.size() < single.size() * 3,
        "merged=" + merged.size() + " single=" + single.size());
  }

  @Test
  void cpuTimeIsPopulatedWhenRequested() {
    Assumptions.assumeTrue(ManagementFactory.getThreadMXBean().isThreadCpuTimeSupported(),
        "this JVM does not support per-thread CPU time");

    // fromSelfWithCpu(1, 0) measures an interval of essentially zero length, so cpu_ns is present
    // but near zero — never a cumulative since-thread-start value. Only presence and sanity are
    // asserted here; the windowed semantics are pinned by cpuNsIsPerIntervalNotSinceThreadStart.
    List<ThreadInfo> threads = Pipeline.fromSelfWithCpu(1, 0).threads();
    assertTrue(threads.stream().anyMatch(t -> t.cpuNs() != null),
        "expected at least one thread carrying cpu_ns");
    assertTrue(threads.stream().allMatch(t -> t.cpuNs() == null || t.cpuNs() >= 0),
        "cpu_ns must never be negative");
  }

  @Test
  void cpuNsIsPerIntervalNotSinceThreadStart() throws InterruptedException {
    Assumptions.assumeTrue(ManagementFactory.getThreadMXBean().isThreadCpuTimeSupported(),
        "this JVM does not support per-thread CPU time");

    AtomicBoolean run = new AtomicBoolean(true);
    Thread busy = new Thread(() -> {
      long sink = 0;
      while (run.get()) {
        sink += ThreadLocalRandom.current().nextLong();   // tight loop, real CPU
      }
      if (sink == Long.MIN_VALUE) {
        System.out.print("");   // keep the loop from being optimised away entirely
      }
    }, "cpu-burner");
    busy.setDaemon(true);
    busy.start();
    try {
      Thread.sleep(500);   // build up lifetime CPU that a cumulative counter would report

      long startNs = System.nanoTime();
      List<ThreadInfo> merged = Pipeline.fromSelfWithCpu(5, 20).merge(Attr.TID).threads();
      long windowNs = System.nanoTime() - startNs;

      ThreadInfo burner = merged.stream()
          .filter(t -> "cpu-burner".equals(t.name()))
          .findFirst().orElseThrow();
      assertNotNull(burner.cpuNs(), "the busy thread should carry cpu_ns");

      // A per-interval delta summed over the window cannot exceed the window itself, let alone the
      // window times every core. Five samples of a cumulative counter (5 x ~0.5s+ of lifetime CPU)
      // blows straight through this bound.
      long ceiling = windowNs * Runtime.getRuntime().availableProcessors();
      assertTrue(burner.cpuNs() <= ceiling,
          "merged cpu_ns " + burner.cpuNs() + " exceeds the sampling window " + windowNs
              + "ns x " + Runtime.getRuntime().availableProcessors()
              + " cores - looks like a cumulative counter, not a per-interval delta");
    } finally {
      run.set(false);
      busy.join(2000);
    }
  }

  @Test
  void cpuTimeIsAbsentWhenNotRequested() {
    assertTrue(Pipeline.fromSelf().threads().stream().allMatch(t -> t.cpuNs() == null));
  }

  @Test
  void headerIsDatePrefixedSoWrittenSnapshotsResplit(@TempDir Path dir) throws IOException {
    // JmxDataSourcePlugin's header uses Date.toString(), which does NOT match the
    // ^\d\d\d\d-\d\d-\d\d pattern the stream parser splits on, so concatenated JMX dumps
    // collapse into one. The in-process source must not repeat that mistake.
    List<ThreadDump> two = Pipeline.fromSelf(2, 0).collect();
    Path file = dir.resolve("self.txt");
    Files.writeString(file, two.get(0) + "\n" + two.get(1) + "\n");

    assertEquals(2, Pipeline.fromFile(file.toString()).collect().size(),
        "two written snapshots must read back as two dumps");
  }

  @Test
  void aNonDaemonThreadSurvivesATextRoundTripAsNonDaemon(@TempDir Path dir) throws IOException {
    // the serializer used to write the daemon marker on presence of the property, so a source
    // storing a real boolean rendered daemon=false threads as daemons
    Assumptions.assumeFalse(Thread.currentThread().isDaemon(),
        "this test needs a non-daemon subject thread");
    String thisThread = Thread.currentThread().getName();

    Path file = dir.resolve("self.txt");
    Files.writeString(file, Pipeline.fromSelf().collectOne().toString() + "\n");

    ThreadInfo me = Pipeline.fromFile(file.toString())
        .contains(Attr.NAME, thisThread)
        .threads().stream().findFirst().orElseThrow();

    assertFalse(me.daemon(), "'" + thisThread + "' is not a daemon thread");
  }

  @Test
  void groupingSelfSnapshotByStateCoversEveryThread() {
    List<ThreadInfo> all = Pipeline.fromSelf().threads();
    List<ThreadInfo> grouped = Pipeline.fromSelf().group(Attr.STATE).threads();

    assertTrue(grouped.size() <= all.size());
    assertTrue(grouped.stream().mapToInt(ThreadInfo::count).sum() > 0);
  }

  @Test
  void invalidSamplingParametersAreRejected() {
    assertThrows(MjprofException.class, () -> Pipeline.fromSelf(0, 0));
    assertThrows(MjprofException.class, () -> Pipeline.fromSelf(-1, 0));
    assertThrows(MjprofException.class, () -> Pipeline.fromSelf(1, -5));
    assertThrows(MjprofException.class, () -> Pipeline.fromSelfWithCpu(0, 0));
  }
}
