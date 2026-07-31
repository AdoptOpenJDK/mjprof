package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.plugins.filters.JStackFilterFieldContains;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.StackTop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineTest {

  private static final String FIXTURE = "src/test/res/test.txt";

  @Test
  void fromFileReadsOneDump() {
    List<ThreadDump> dumps = Pipeline.fromFile(FIXTURE).collect();
    assertEquals(1, dumps.size());
    assertFalse(dumps.get(0).getThreadInfos().isEmpty());
  }

  @Test
  void collectOneReturnsTheSingleDump() {
    ThreadDump d = Pipeline.fromFile(FIXTURE).collectOne();
    assertFalse(d.getThreadInfos().isEmpty());
  }

  @Test
  void threadsFlattensDumps() {
    List<ThreadInfo> threads = Pipeline.fromFile(FIXTURE).threads();
    assertEquals(Pipeline.fromFile(FIXTURE).collectOne().getThreadInfos().size(), threads.size());
  }

  @Test
  void mapAcceptsAFilterInstance() {
    List<ThreadInfo> mains = Pipeline.fromFile(FIXTURE)
        .map(new JStackFilterFieldContains(Attr.NAME.getAttrName(), "main"))
        .threads();
    assertFalse(mains.isEmpty());
    assertTrue(mains.stream().allMatch(t -> t.name().contains("main")));
  }

  @Test
  void mapAcceptsASingleThreadMapperInstance() {
    List<ThreadInfo> trimmed = Pipeline.fromFile(FIXTURE)
        .map(new StackTop(1))
        .threads();
    assertFalse(trimmed.isEmpty());
  }

  @Test
  void stagesRunInOrderAndPipelineIsReusable() {
    Pipeline base = Pipeline.fromFile(FIXTURE);
    int all = base.threads().size();
    int filtered = Pipeline.fromFile(FIXTURE)
        .map(new JStackFilterFieldContains(Attr.NAME.getAttrName(), "main"))
        .threads().size();
    assertTrue(filtered < all, "filter must remove threads");
    assertEquals(all, base.threads().size(), "collecting twice must give the same result");
  }

  @Test
  void missingFileThrowsMjprofException() {
    MjprofException e = assertThrows(MjprofException.class,
        () -> Pipeline.fromFile("no/such/file.txt").collect());
    assertTrue(e.getMessage().contains("no/such/file.txt")
        || e.getCause().getMessage().contains("no/such/file.txt"), e.getMessage());
  }

  @Test
  @Timeout(30)
  void unreachableJmxThrowsMjprofException() {
    // well-formed host:port, but nothing is listening on port 1 - the genuine connect-failure path
    assertThrows(MjprofException.class,
        () -> Pipeline.fromJmx("localhost:1", 1, 0, "", "").collect());
  }

  @Test
  void nullJmxCredentialsAreAcceptedAsNoAuth() {
    // building the Pipeline must not throw - null user/pass means "no credentials", not a
    // programmer error. If Finding 2's normalization regressed, this line itself would NPE.
    Pipeline pipeline = Pipeline.fromJmx("localhost:1", 1, 0, null, null);

    // Nothing listens on port 1, so collecting must fail - but with MjprofException wrapping the
    // real connect failure, not a NullPointerException from unnormalized credentials. A bare
    // assertThrows(MjprofException.class, ...) would not catch a regression here, since an NPE
    // wrapped as MjprofException also satisfies it - so inspect the cause explicitly.
    MjprofException e = assertThrows(MjprofException.class, pipeline::collect);
    Throwable cause = e.getCause();
    assertFalse(cause instanceof NullPointerException,
        "credentials must be normalized before reaching the plugin, not NPE: " + e);
  }

  @Test
  void malformedJmxHostPortThrowsMjprofExceptionWithoutExiting() {
    // JMXServiceURL is lenient about host/port syntax (e.g. "host:" or "host:port" both parse), but
    // rejects any non-ASCII/control character in the URL outright - a newline in the host:port
    // reliably reproduces a MalformedURLException without needing a live network attempt.
    assertThrows(MjprofException.class,
        () -> Pipeline.fromJmx("bad\nhost:1", 1, 0, "", ""));
  }
}
