package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins how dumps persisted as text split back apart, so the JMX-header limitation is a known, tested
 * boundary rather than a surprise. Numeric-property fidelity is covered by ThreadInfoAccessorsTest.
 */
class TextRoundTripLimitsTest {

  private static ThreadDump dumpWith(String headerText, String threadName) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, threadName);
    ti.setVal(ThreadInfoProps.TID, "0x00007f");
    ti.setVal(ThreadInfoProps.STATE, "RUNNABLE");
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader(headerText));
    ArrayList<ThreadInfo> tis = new ArrayList<>();
    tis.add(ti);
    d.setThreadInfos(tis);
    return d;
  }

  private static Path write(Path dir, String name, ThreadDump... dumps) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (ThreadDump d : dumps) {
      sb.append(d).append("\n");
    }
    Path f = dir.resolve(name);
    Files.writeString(f, sb.toString());
    return f;
  }

  @Test
  void jstackStyleHeadersSplitBackIntoSeparateDumps(@TempDir Path dir) throws IOException {
    Path f = write(dir, "jstack.txt",
        dumpWith("2026-07-31 10:00:00\nFull thread dump", "worker-a"),
        dumpWith("2026-07-31 10:00:05\nFull thread dump", "worker-b"));

    List<ThreadDump> read = Pipeline.fromFile(f.toString()).collect();
    assertEquals(2, read.size(), "date-prefixed headers must split into separate dumps");
  }

  @Test
  void jmxStyleHeadersDoNotSplit(@TempDir Path dir) throws IOException {
    // exactly what JmxDataSourcePlugin writes: Date.toString(), which is not date-prefixed
    String jmxHeader = new Date() + "\nThread dump via JMX of process localhost:9010";
    Path f = write(dir, "jmx.txt",
        dumpWith(jmxHeader, "worker-a"),
        dumpWith(jmxHeader, "worker-b"));

    List<ThreadDump> read = Pipeline.fromFile(f.toString()).collect();
    assertEquals(1, read.size(),
        "JMX headers are not date-prefixed, so concatenated JMX dumps collapse into one — "
            + "use Pipeline.fromDumps to keep self-collected samples separate");
  }
}
