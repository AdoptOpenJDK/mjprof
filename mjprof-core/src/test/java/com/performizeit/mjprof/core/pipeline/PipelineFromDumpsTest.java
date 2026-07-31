package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineFromDumpsTest {

  private static ThreadDump sample(String tid, long cpuNs) {
    return sample("worker", tid, cpuNs);
  }

  private static ThreadDump sample(String name, String tid, long cpuNs) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, name);
    ti.setVal(ThreadInfoProps.TID, tid);
    ti.setVal(ThreadInfoProps.CPUNS, cpuNs);
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader("2026-07-31 10:00:00"));
    ArrayList<ThreadInfo> tis = new ArrayList<>();
    tis.add(ti);
    d.setThreadInfos(tis);
    return d;
  }

  @Test
  void fromDumpsEmitsOneResultPerDump() {
    List<ThreadDump> in = List.of(sample("0x1", 10), sample("0x1", 20), sample("0x1", 30));
    assertEquals(3, Pipeline.fromDumps(in).collect().size());
  }

  @Test
  void mergeSumsCpuAcrossSelfCollectedSamples() {
    List<ThreadDump> samples = List.of(sample("0x1", 10), sample("0x1", 20), sample("0x1", 30));

    List<ThreadInfo> merged = Pipeline.fromDumps(samples).merge(Attr.TID).threads();

    assertEquals(1, merged.size(), "same tid across samples folds into one entry");
    assertEquals(60L, merged.get(0).cpuNs(), "cpu_ns is summed across samples");
    assertEquals(3, merged.get(0).count());
  }

  @Test
  void mergeKeepsDistinctThreadsApart() {
    List<ThreadDump> samples = List.of(sample("0x1", 10), sample("0x2", 20));
    assertEquals(2, Pipeline.fromDumps(samples).merge(Attr.TID).threads().size());
  }

  @Test
  void emptyCollectionYieldsNoDumps() {
    assertTrue(Pipeline.fromDumps(List.of()).collect().isEmpty());
  }

  @Test
  void nullCollectionIsRejected() {
    assertThrows(MjprofException.class, () -> Pipeline.fromDumps(null));
  }

  @Test
  void collectingTwiceGivesTheSameResult() {
    Pipeline p = Pipeline.fromDumps(List.of(sample("0x1", 10), sample("0x1", 20)));
    assertEquals(2, p.collect().size());
    assertEquals(2, p.collect().size());
  }

  // -- deferred gap (Task 4): threadsFlattensDumps in PipelineTest is tautological against a
  // single-dump fixture. Here, a genuine multi-dump source proves threads() flattens every dump,
  // not just the first: three dumps of one thread each must yield three threads.
  @Test
  void threadsFlattensMultipleDumpsNotJustTheFirst() {
    List<ThreadDump> in = List.of(sample("a", "0x1", 1), sample("b", "0x2", 2), sample("c", "0x3", 3));
    List<ThreadInfo> threads = Pipeline.fromDumps(in).threads();

    assertEquals(3, threads.size(), "threads() must flatten every dump, not only the first");
    assertEquals(List.of("a", "b", "c"), threads.stream().map(ThreadInfo::name).toList());
  }

  // -- deferred gap (Task 5): no existing test put Attr.STACK in a non-first varargs position, so a
  // mutant guard that only checks attrs[0] would slip through.
  @Test
  void groupRejectsStackInANonFirstPosition() {
    MjprofException e = assertThrows(MjprofException.class,
        () -> Pipeline.fromDumps(List.of(sample("0x1", 1))).group(Attr.NAME, Attr.STACK));
    assertTrue(e.getMessage().contains("tree()"), e.getMessage());
  }

  // -- deferred gap (Task 5): no test chained merge() with a following stage, so the runner's
  // handleDone fan-forward was only exercised at the PipelineRunner level, not through the public
  // Pipeline API. Three samples of two distinct tids merge to two entries, then a following filter
  // narrows that merged result down to one — provable only if the merged output actually flowed
  // through the later stage.
  @Test
  void mergeFollowedByAFilterSeesTheMergedResult() {
    List<ThreadDump> samples = List.of(
        sample("keep", "0x1", 10),
        sample("drop", "0x2", 20),
        sample("keep", "0x1", 30));

    List<ThreadInfo> result = Pipeline.fromDumps(samples)
        .merge(Attr.TID)
        .contains(Attr.NAME, "keep")
        .threads();

    assertEquals(1, result.size(), "the filter after merge() must see the merged, not raw, threads");
    assertEquals("keep", result.get(0).name());
    assertEquals(40L, result.get(0).cpuNs(), "merged cpu_ns for the surviving tid across its 2 samples");
  }
}
