package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;
import com.performizeit.mjprof.core.plumbing.PipeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a source and a chain of stages synchronously on the calling thread.
 *
 * <p>Mirrors the semantics of the threaded Generator/Pipe plumbing used by the command line: each
 * dump produced by the source is pushed through every stage in order, a stage returning null drops
 * the message, and once the source is exhausted each stage's handleDone output is pushed through the
 * stages after it.
 */
final class PipelineRunner {

  private PipelineRunner() {
  }

  static List<ThreadDump> run(GeneratorHandler<ThreadDump> source,
                              List<PipeHandler<ThreadDump, ThreadDump>> stages) {
    List<ThreadDump> results = new ArrayList<>();

    while (!isDone(source)) {
      ThreadDump generated = generate(source);
      if (generated != null) {
        push(generated, 0, stages, results);
      }
      if (!isDone(source)) {
        sleep(source);
      }
    }

    for (int i = 0; i < stages.size(); i++) {
      ThreadDump done = handleDone(stages.get(i));
      if (done != null) {
        push(done, i + 1, stages, results);
      }
    }
    return results;
  }

  private static void push(ThreadDump dump, int fromStage,
                           List<PipeHandler<ThreadDump, ThreadDump>> stages,
                           List<ThreadDump> results) {
    ThreadDump current = dump;
    for (int i = fromStage; i < stages.size(); i++) {
      PipeHandler<ThreadDump, ThreadDump> stage = stages.get(i);
      try {
        current = stage.handleMsg(current);
      } catch (RuntimeException e) {
        throw new MjprofException(
            "stage " + stage.getClass().getName() + " failed: " + e.getMessage(), e);
      }
      if (current == null) {
        return;
      }
    }
    results.add(current);
  }

  private static ThreadDump generate(GeneratorHandler<ThreadDump> source) {
    try {
      return source.generate();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "data source " + source.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }

  private static boolean isDone(GeneratorHandler<ThreadDump> source) {
    try {
      return source.isDone();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "data source " + source.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }

  private static void sleep(GeneratorHandler<ThreadDump> source) {
    try {
      source.sleepBetweenIteration();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "data source " + source.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }

  private static ThreadDump handleDone(PipeHandler<ThreadDump, ThreadDump> stage) {
    try {
      return stage.handleDone();
    } catch (RuntimeException e) {
      throw new MjprofException(
          "stage " + stage.getClass().getName() + " failed: " + e.getMessage(), e);
    }
  }
}
