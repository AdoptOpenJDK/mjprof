package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.model.JStackHeader;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.parser.ThreadInfoProps;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;
import com.performizeit.mjprof.core.plumbing.PipeHandler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineRunnerTest {

  /** Minimal source over a fixed list of dumps. */
  private static GeneratorHandler<ThreadDump> source(ThreadDump... dumps) {
    Iterator<ThreadDump> it = List.of(dumps).iterator();
    return new GeneratorHandler<>() {
      public ThreadDump generate() {
        return it.hasNext() ? it.next() : null;
      }

      public boolean isDone() {
        return !it.hasNext();
      }

      public void sleepBetweenIteration() {
      }
    };
  }

  private static ThreadDump dumpNamed(String threadName) {
    ThreadInfo ti = new ThreadInfo();
    ti.setVal(ThreadInfoProps.NAME, threadName);
    ThreadDump d = new ThreadDump();
    d.setHeader(new JStackHeader("2026-07-31 10:00:00"));
    ArrayList<ThreadInfo> tis = new ArrayList<>();
    tis.add(ti);
    d.setThreadInfos(tis);
    return d;
  }

  /** Passes every dump through untouched. */
  private static PipeHandler<ThreadDump, ThreadDump> passThrough() {
    return new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        return msg;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };
  }

  /** Swallows every dump, emits a single summary dump on done — the AccumulateDumps shape. */
  private static PipeHandler<ThreadDump, ThreadDump> collectOnDone() {
    List<ThreadDump> seen = new ArrayList<>();
    return new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        seen.add(msg);
        return null;
      }

      public ThreadDump handleDone() {
        return dumpNamed("merged-" + seen.size());
      }
    };
  }

  @Test
  void emitsOneResultPerSourceDump() {
    List<ThreadDump> out = PipelineRunner.run(source(dumpNamed("a"), dumpNamed("b")), List.of());
    assertEquals(2, out.size());
    assertEquals("a", out.get(0).getThreadInfos().get(0).name());
    assertEquals("b", out.get(1).getThreadInfos().get(0).name());
  }

  @Test
  void stageReturningNullDropsTheDump() {
    PipeHandler<ThreadDump, ThreadDump> dropAll = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        return null;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };
    List<ThreadDump> out = PipelineRunner.run(source(dumpNamed("a")), List.of(dropAll));
    assertTrue(out.isEmpty());
  }

  @Test
  void handleDoneOutputFlowsThroughRemainingStages() {
    // stage 0 accumulates and emits on done; stage 1 must see that emitted dump
    List<String> namesSeenByStage1 = new ArrayList<>();
    PipeHandler<ThreadDump, ThreadDump> recorder = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        namesSeenByStage1.add(msg.getThreadInfos().get(0).name());
        return msg;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };

    List<ThreadDump> out = PipelineRunner.run(
        source(dumpNamed("a"), dumpNamed("b"), dumpNamed("c")),
        List.of(collectOnDone(), recorder));

    assertEquals(List.of("merged-3"), namesSeenByStage1,
        "the done-output of stage 0 must be pushed through stage 1");
    assertEquals(1, out.size());
    assertEquals("merged-3", out.get(0).getThreadInfos().get(0).name());
  }

  @Test
  void doneOutputDoesNotReenterEarlierStages() {
    // stage 1 emits on done; stage 0 must not see it (no loops)
    List<String> namesSeenByStage0 = new ArrayList<>();
    PipeHandler<ThreadDump, ThreadDump> recorder = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        namesSeenByStage0.add(msg.getThreadInfos().get(0).name());
        return msg;
      }

      public ThreadDump handleDone() {
        return null;
      }
    };

    PipelineRunner.run(source(dumpNamed("a")), List.of(recorder, collectOnDone()));

    assertEquals(List.of("a"), namesSeenByStage0);
  }

  @Test
  void sourceFailureIsWrappedWithTheFailingClassName() {
    GeneratorHandler<ThreadDump> boom = new GeneratorHandler<>() {
      public ThreadDump generate() {
        throw new IllegalStateException("no such file");
      }

      public boolean isDone() {
        return false;
      }

      public void sleepBetweenIteration() {
      }
    };

    MjprofException e = assertThrows(MjprofException.class,
        () -> PipelineRunner.run(boom, List.of()));
    assertTrue(e.getMessage().contains("no such file"), e.getMessage());
    assertEquals(IllegalStateException.class, e.getCause().getClass());
  }

  @Test
  void stageFailureIsWrappedWithTheFailingStageClassName() {
    PipeHandler<ThreadDump, ThreadDump> boom = new PipeHandler<>() {
      public ThreadDump handleMsg(ThreadDump msg) {
        throw new IllegalArgumentException("bad prop");
      }

      public ThreadDump handleDone() {
        return null;
      }
    };

    MjprofException e = assertThrows(MjprofException.class,
        () -> PipelineRunner.run(source(dumpNamed("a")), List.of(boom)));
    assertTrue(e.getMessage().contains("bad prop"), e.getMessage());
  }

  @Test
  void sleepBetweenIterationIsCalledOncePerNonFinalDump() {
    int[] sleeps = {0};
    Iterator<ThreadDump> it = List.of(dumpNamed("a"), dumpNamed("b")).iterator();
    GeneratorHandler<ThreadDump> counting = new GeneratorHandler<>() {
      public ThreadDump generate() {
        return it.hasNext() ? it.next() : null;
      }

      public boolean isDone() {
        return !it.hasNext();
      }

      public void sleepBetweenIteration() {
        sleeps[0]++;
      }
    };

    PipelineRunner.run(counting, List.of(passThrough()));
    assertEquals(1, sleeps[0], "sleep only between dumps, never after the last");
  }
}
