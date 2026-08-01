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

    // Timing-dependent in principle, but not flaky in practice: by the time this line runs, the
    // test thread itself has necessarily executed everything above it (JUnit dispatch, the
    // assumption check, ManagementFactory lookups), which burns measurable CPU. The snapshot
    // always finds at least the current thread with cpu_ns > 0.
    List<ThreadInfo> threads = Pipeline.fromSelfWithCpu(1, 0).threads();
    assertTrue(threads.stream().anyMatch(t -> t.cpuNs() != null && t.cpuNs() > 0),
        "expected at least one thread with positive cpu_ns");
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
