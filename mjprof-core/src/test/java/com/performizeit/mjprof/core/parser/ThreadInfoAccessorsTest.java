package com.performizeit.mjprof.core.parser;

import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.model.Profile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @Test
  void stringCoercionForNumericProps() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.CPUNS, "1500");
    ti.setVal(ThreadInfoProps.WALL, "40");
    ti.setVal(ThreadInfoProps.CPU_PREC, "3.75");
    ti.setVal(ThreadInfoProps.COUNT, "4");

    assertEquals(1500L, ti.cpuNs(), "cpuNs should coerce String to Long");
    assertEquals(40L, ti.wallMs(), "wallMs should coerce String to Long");
    assertEquals(3.75, ti.cpuPct(), "cpuPct should coerce String to Double");
    assertEquals(4, ti.count(), "count should coerce String to int");
  }

  @Test
  void integerToLongCoercion() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.CPUNS, 1500);  // Integer, not Long
    assertEquals(1500L, ti.cpuNs(), "cpuNs should coerce Integer to Long");
  }

  @Test
  void nonNumericStringThrowsNumberFormatException() {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.CPUNS, "not-a-number");
    assertThrows(NumberFormatException.class, ti::cpuNs, "cpuNs should throw for non-numeric String");
  }

  @Test
  void roundTripPreservesNumericValues() {
    // Build a ThreadInfo with numeric values (as they come from JMX)
    ThreadInfo original = new ThreadInfo();
    original.setVal(ThreadInfoProps.NAME, "worker-1");
    original.setVal(ThreadInfoProps.TID, "0x1234");
    original.setVal(ThreadInfoProps.CPUNS, 1_500L);
    original.setVal(ThreadInfoProps.WALL, 40L);
    original.setVal(ThreadInfoProps.CPU_PREC, 3.75);
    original.setVal(ThreadInfoProps.COUNT, 4);

    // Wrap in ThreadDump and render to text (simulates toString path)
    JStackHeader header = new JStackHeader("2025-01-15 10:30:45");
    ThreadDump dump = new ThreadDump(header, new ArrayList<>() {{ add(original); }});
    String textForm = dump.toString();

    // Re-parse (this is the path that puts values back as Strings)
    ThreadDump reparsed = ThreadDumpTextualParser.parseStringRepresentation(textForm);

    // The test must fail if fidelity breaks: no null-guards, no isEmpty() escape hatch.
    assertEquals(1, reparsed.getThreadInfos().size(), "re-parsing must yield exactly one thread");
    ThreadInfo roundTripped = reparsed.getThreadInfos().get(0);
    assertEquals("worker-1", roundTripped.name(), "name should survive round-trip");
    assertEquals("0x1234", roundTripped.tid(), "tid should survive round-trip");
    assertEquals(4, roundTripped.count(), "count should survive round-trip");
    assertEquals(1_500L, roundTripped.cpuNs(), "cpuNs should coerce from String in round-trip");
    assertEquals(40L, roundTripped.wallMs(), "wallMs should coerce from String in round-trip");
    assertEquals(3.75, roundTripped.cpuPct(), "cpuPct should coerce from String in round-trip");
  }
}
