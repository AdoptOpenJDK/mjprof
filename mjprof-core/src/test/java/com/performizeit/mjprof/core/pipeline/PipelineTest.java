package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.plugins.filters.JStackFilterFieldContains;
import com.performizeit.mjprof.core.plugins.mappers.singlethread.StackTop;
import org.junit.jupiter.api.Test;

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
  void unreachableJmxThrowsMjprofException() {
    // port 1 is never a live JMX endpoint
    assertThrows(MjprofException.class,
        () -> Pipeline.fromJmx("localhost:1", 1, 0, null, null).collect());
  }
}
