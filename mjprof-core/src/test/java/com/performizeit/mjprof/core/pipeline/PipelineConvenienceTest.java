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
  void treeMergedEntryActuallyContainsTheStackContent() {
    // size() == 1 alone would also be true if TreeMergeStage's Profile round-trip silently produced
    // a bogus single-frame profile -- assert the merge actually carried real frames from the fixture.
    ThreadInfo merged = fixture().tree().threads().get(0);
    String rendered = merged.stack().toString();
    assertFalse(rendered.isBlank());
    assertTrue(rendered.contains("WorkerPool.startJob"));
  }

  @Test
  void chainingAnAttributeFilterAfterTreeDropsTheSyntheticEntry() {
    // TreeMergeStage.handleDone() synthesizes a ThreadDump whose sole ThreadInfo has only STACK
    // set -- name/tid/state are all null, by design (there is no single "name" for a merged tree of
    // every thread). So any attribute filter chained after tree() drops that entry: documented
    // behavior, not a bug to paper over with an invented name/tid/state for the synthetic entry.
    List<ThreadInfo> t = fixture().tree().contains(Attr.NAME, "main").threads();
    assertTrue(t.isEmpty());
  }

  @Test
  void treeTextReturnsTheMergedTreeAsPlainStringWithoutChaining() {
    String text = fixture().treeText();
    assertFalse(text.isBlank());
    assertTrue(text.contains("WorkerPool.startJob"));
  }

  @Test
  void chainingSeveralConvenienceStagesWorks() {
    List<ThreadInfo> t = fixture()
        .contains(Attr.STACK, "at")
        .withoutNameSuffix()
        .group(Attr.NAME)
        .sortDesc(Attr.COUNT)
        .threads();
    assertFalse(t.isEmpty());
  }

  @Test
  void sortDescByCountPutsEntriesMissingCountLast() {
    // group(Attr.NAME) leaves singleton groups (most thread names in a real dump are unique)
    // without a count property at all -- ThreadInfoAggregator only sets COUNT once an actual merge
    // happens. PropComparator/ReversePropComparator must treat that as "sorts last", not throw.
    List<ThreadInfo> t = fixture()
        .contains(Attr.STACK, "at")
        .withoutNameSuffix()
        .group(Attr.NAME)
        .sortDesc(Attr.COUNT)
        .threads();
    assertFalse(t.isEmpty());
    assertTrue(t.stream().anyMatch(x -> x.getVal(Attr.COUNT.getAttrName()) == null),
        "expected at least one singleton group with no count property");
    ThreadInfo last = t.get(t.size() - 1);
    assertEquals(null, last.getVal(Attr.COUNT.getAttrName()));
  }

  @Test
  void sortAscByCountAlsoPutsEntriesMissingCountLast() {
    List<ThreadInfo> t = fixture()
        .contains(Attr.STACK, "at")
        .withoutNameSuffix()
        .group(Attr.NAME)
        .sort(Attr.COUNT)
        .threads();
    assertFalse(t.isEmpty());
    ThreadInfo last = t.get(t.size() - 1);
    assertEquals(null, last.getVal(Attr.COUNT.getAttrName()));
  }
}
