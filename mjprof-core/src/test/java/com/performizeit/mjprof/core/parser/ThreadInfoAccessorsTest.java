package com.performizeit.mjprof.core.parser;

import com.performizeit.mjprof.core.model.Profile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreadInfoAccessorsTest {

  private ThreadInfo sample() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, "worker-1");
    ti.setVal(ThreadInfoProps.STATE, "RUNNABLE");
    ti.setVal(ThreadInfoProps.STATUS, "runnable");
    ti.setVal(ThreadInfoProps.TID, "0x00007f");
    ti.setVal(ThreadInfoProps.CPUNS, 1_500L);
    ti.setVal(ThreadInfoProps.WALL, 40L);
    ti.setVal(ThreadInfoProps.CPU_PREC, 3.75);
    ti.setVal(ThreadInfoProps.COUNT, 4);
    ti.setVal(ThreadInfoProps.DAEMON, true);
    return ti;
  }

  @Test
  void accessorsReadTypedValues() {
    ThreadInfo ti = sample();
    assertEquals("worker-1", ti.name());
    assertEquals("RUNNABLE", ti.state());
    assertEquals("runnable", ti.status());
    assertEquals("0x00007f", ti.tid());
    assertEquals(1_500L, ti.cpuNs());
    assertEquals(40L, ti.wallMs());
    assertEquals(3.75, ti.cpuPct());
    assertEquals(4, ti.count());
    assertTrue(ti.daemon());
  }

  @Test
  void missingPropsAreNullAndCountDefaultsToOne() {
    ThreadInfo empty = new ThreadInfo();
    assertNull(empty.name());
    assertNull(empty.cpuNs());
    assertNull(empty.cpuPct());
    assertFalse(empty.daemon());
    assertEquals(1, empty.count(), "a thread with no count prop represents one thread");
  }

  @Test
  void stackReturnsProfileInstance() {
    ThreadInfo ti = new ThreadInfo();
    Profile profile = new Profile();
    ti.setVal(ThreadInfoProps.STACK, profile);
    assertSame(profile, ti.stack());
  }

  @Test
  void stackReturnsNullWhenAbsent() {
    ThreadInfo ti = new ThreadInfo();
    assertNull(ti.stack());
  }
}
