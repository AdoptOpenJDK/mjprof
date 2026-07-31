package com.performizeit.mjprof.core.plugins.mappers;

import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupByPropMultiAttrTest {

  private static ThreadInfo thread(String name, String state) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, name);
    ti.setVal(ThreadInfoProps.STATE, state);
    return ti;
  }

  private static ThreadDump dumpOf(ThreadInfo... threads) {
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader("2026-07-31 10:00:00"));
    d.setThreadInfos(new ArrayList<>(List.of(threads)));
    return d;
  }

  @Test
  void singleAttrGroupsByThatAttrOnly() {
    ThreadDump grouped = new GroupByProp(List.of(ThreadInfoProps.STATE)).map(dumpOf(
        thread("a", "RUNNABLE"),
        thread("b", "RUNNABLE"),
        thread("c", "BLOCKED")));

    assertEquals(2, grouped.getThreadInfos().size());
    assertEquals(3, grouped.getThreadInfos().stream().mapToInt(ThreadInfo::count).sum());
  }

  @Test
  void twoAttrsGroupOnTheCompositeKey() {
    ThreadDump grouped = new GroupByProp(List.of(ThreadInfoProps.STATE, ThreadInfoProps.NAME)).map(dumpOf(
        thread("a", "RUNNABLE"),
        thread("a", "RUNNABLE"),
        thread("b", "RUNNABLE"),
        thread("b", "BLOCKED")));

    assertEquals(3, grouped.getThreadInfos().size(),
        "(RUNNABLE,a) (RUNNABLE,b) (BLOCKED,b)");
    assertEquals(4, grouped.getThreadInfos().stream().mapToInt(ThreadInfo::count).sum());
  }

  @Test
  void stringConstructorStillBehavesAsBefore() {
    ThreadDump grouped = new GroupByProp(ThreadInfoProps.STATE).map(dumpOf(
        thread("a", "RUNNABLE"),
        thread("b", "RUNNABLE")));
    assertEquals(1, grouped.getThreadInfos().size());
  }
}
