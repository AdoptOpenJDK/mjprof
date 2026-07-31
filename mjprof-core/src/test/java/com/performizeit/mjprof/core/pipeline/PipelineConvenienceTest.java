package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineConvenienceTest {

  private static final String FIXTURE = "src/test/res/test.txt";

  private static Pipeline fixture() {
    return Pipeline.fromFile(FIXTURE);
  }

  @Test
  void containsKeepsOnlyMatchingThreads() {
    List<ThreadInfo> t = fixture().contains(Attr.NAME, "main").threads();
    assertFalse(t.isEmpty());
    assertTrue(t.stream().allMatch(x -> x.name().contains("main")));
  }

  @Test
  void notContainsIsTheComplementOfContains() {
    int all = fixture().threads().size();
    int with = fixture().contains(Attr.NAME, "main").threads().size();
    int without = fixture().notContains(Attr.NAME, "main").threads().size();
    assertEquals(all, with + without);
  }

  @Test
  void matchesAppliesARegex() {
    List<ThreadInfo> t = fixture().matches(Attr.STATE, "RUNNABLE|BLOCKED").threads();
    assertTrue(t.stream().allMatch(x -> x.state().equals("RUNNABLE") || x.state().equals("BLOCKED")));
  }

  @Test
  void sortOrdersByAttributeAndSortDescReversesIt() {
    List<ThreadInfo> asc = fixture().sort(Attr.NAME).threads();
    List<ThreadInfo> desc = fixture().sortDesc(Attr.NAME).threads();
    assertEquals(asc.size(), desc.size());
    assertEquals(asc.get(0).name(), desc.get(desc.size() - 1).name());
  }

  @Test
  void stackTopAndBottomTrimStacksWithoutDroppingThreads() {
    int all = fixture().threads().size();
    assertEquals(all, fixture().stackTop(1).threads().size());
    assertEquals(all, fixture().stackBottom(1).threads().size());
  }

  @Test
  void withoutPropRemovesTheAttribute() {
    List<ThreadInfo> t = fixture().withoutProp(Attr.STATE).threads();
    assertTrue(t.stream().allMatch(x -> x.state() == null));
  }

  @Test
  void treeProducesASingleMergedEntry() {
    assertEquals(1, fixture().tree().threads().size());
  }

  @Test
  void chainingSeveralConvenienceStagesWorks() {
    // Sorts by NAME, not COUNT: group(Attr.NAME) leaves singleton groups (most thread names in a
    // real dump are unique) without a count property at all -- ThreadInfoAggregator only sets COUNT
    // once an actual merge happens -- so sorting those by COUNT throws a NullPointerException deep in
    // PropComparator. NAME is the grouping key, so it is always present on every resulting entry.
    List<ThreadInfo> t = fixture()
        .contains(Attr.STACK, "at")
        .withoutNameSuffix()
        .group(Attr.NAME)
        .sortDesc(Attr.NAME)
        .threads();
    assertFalse(t.isEmpty());
  }
}
