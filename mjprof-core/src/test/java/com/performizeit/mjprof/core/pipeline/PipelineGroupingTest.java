package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineGroupingTest {

  private static final String FIXTURE = "src/test/res/test.txt";

  @Test
  void groupByStateCollapsesThreadsAndPreservesTheTotalCount() {
    List<ThreadInfo> ungrouped = Pipeline.fromFile(FIXTURE).threads();
    List<ThreadInfo> grouped = Pipeline.fromFile(FIXTURE).group(Attr.STATE).threads();

    assertTrue(grouped.size() < ungrouped.size(), "grouping must collapse threads");
    assertEquals(ungrouped.size(), grouped.stream().mapToInt(ThreadInfo::count).sum());
  }

  @Test
  void groupWithNoAttrsMergesTheWholeDump() {
    assertEquals(1, Pipeline.fromFile(FIXTURE).group().threads().size());
  }

  @Test
  void groupByStackIsRejected() {
    MjprofException e = assertThrows(MjprofException.class,
        () -> Pipeline.fromFile(FIXTURE).group(Attr.STACK));
    assertTrue(e.getMessage().contains("tree()"), e.getMessage());
  }

  @Test
  void mergeByStackIsRejected() {
    assertThrows(MjprofException.class,
        () -> Pipeline.fromFile(FIXTURE).merge(Attr.STACK));
  }

  @Test
  void mergeOfASingleDumpKeepsEveryThread() {
    // merge keys on tid by default; one dump means nothing to combine
    List<ThreadInfo> merged = Pipeline.fromFile(FIXTURE).merge().threads();
    assertEquals(Pipeline.fromFile(FIXTURE).threads().size(), merged.size());
  }
}
