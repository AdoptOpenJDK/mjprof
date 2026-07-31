package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.model.Profile;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

  /** A single-thread dump whose stack is {@code frames} deep, top frame first (as in jstack text). */
  private static ThreadDump sampleWithStack(String tid, String... frames) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, "worker");
    ti.setVal(ThreadInfoProps.TID, tid);
    Profile p = new Profile();
    p.addSingle(String.join("\n", frames));
    ti.setVal(ThreadInfoProps.STACK, p);
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

  // I2: a null element inside the collection used to surface as a message-less NPE from
  // List.copyOf. It must instead be a MjprofException that explains the actual hazard.
  @Test
  void nullElementInCollectionIsRejectedWithAnExplanation() {
    List<ThreadDump> withNull = new ArrayList<>();
    withNull.add(sample("0x1", 10));
    withNull.add(null);

    MjprofException e = assertThrows(MjprofException.class, () -> Pipeline.fromDumps(withNull));
    assertTrue(e.getMessage().contains("null"), e.getMessage());
  }

  // C1: fromDumps hands stages the caller's own ThreadDump/ThreadInfo/Profile objects, and several
  // stages (stackTop among them) mutate those objects in place instead of copying them. So a Pipeline
  // built over fromDumps is NOT safely re-collectible when it contains such a stage: the second
  // collect() does not see a fresh copy of the input, it sees whatever the first run left behind, and
  // so does the caller's own retained list. This test observes and documents that, rather than
  // asserting the (false) reuse guarantee.
  @Test
  void collectingTwiceThroughAMutatingStageSeesMutatedInput() {
    ThreadDump dump = sampleWithStack("0x1", "top", "middle", "bottom");
    List<ThreadDump> samples = new ArrayList<>(List.of(dump));

    Pipeline p = Pipeline.fromDumps(samples).stackTop(1);

    String firstRun = stackTextOf(p.collect());
    assertFalse(firstRun.contains("bottom"), "stackTop(1) keeps only the top frame");
    assertFalse(firstRun.contains("middle"), "stackTop(1) keeps only the top frame");
    assertTrue(firstRun.contains("top"), "the top frame itself must survive");

    // The caller's own retained ThreadDump - the exact object passed into fromDumps, still sitting
    // in `samples` - was mutated by the single collect() above. This is the observed hazard: the
    // class Javadoc's "constructed fresh per run" promise does not hold for fromDumps, because the
    // *stage* mutates what fromDumps hands out, not the source.
    String samplesStackNow = dump.getThreadInfos().get(0).stack().toString();
    assertFalse(samplesStackNow.contains("bottom"),
        "the caller's own retained dump lost its lower frames after just one collect()");

    // Consequently, a second collect() on the same Pipeline does not re-process a pristine 3-frame
    // stack - it observes the already-truncated tree left behind by the first run.
    String secondRun = stackTextOf(p.collect());
    assertEquals(firstRun, secondRun,
        "the second collect() sees the same already-mutated stack as the first, not a fresh copy "
            + "of the original 3-frame input");
  }

  private static String stackTextOf(List<ThreadDump> dumps) {
    return dumps.get(0).getThreadInfos().get(0).stack().toString();
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
