package com.performizeit.mjprof.core.api;

import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttrTest {

  @Test
  void constantsMatchThreadInfoProps() {
    assertEquals(ThreadInfoProps.NAME, Attr.NAME.getAttrName());
    assertEquals(ThreadInfoProps.TID, Attr.TID.getAttrName());
    assertEquals(ThreadInfoProps.NID, Attr.NID.getAttrName());
    assertEquals(ThreadInfoProps.DAEMON, Attr.DAEMON.getAttrName());
    assertEquals(ThreadInfoProps.PRIO, Attr.PRIO.getAttrName());
    assertEquals(ThreadInfoProps.STATUS, Attr.STATUS.getAttrName());
    assertEquals(ThreadInfoProps.STATE, Attr.STATE.getAttrName());
    assertEquals(ThreadInfoProps.CPUNS, Attr.CPU_NS.getAttrName());
    assertEquals(ThreadInfoProps.WALL, Attr.WALL_MS.getAttrName());
    assertEquals(ThreadInfoProps.CPU_PREC, Attr.CPU_PCT.getAttrName());
    assertEquals(ThreadInfoProps.STACK, Attr.STACK.getAttrName());
    assertEquals(ThreadInfoProps.LOS, Attr.LOS.getAttrName());
    assertEquals(ThreadInfoProps.COUNT, Attr.COUNT.getAttrName());
  }

  @Test
  void customAttrStillWorks() {
    assertEquals("whatever", new Attr("whatever").getAttrName());
  }

  @Test
  void equalAttrNamesAreEqual() {
    assertEquals(new Attr("state"), Attr.STATE);
    assertEquals(new Attr("state").hashCode(), Attr.STATE.hashCode());
  }
}
