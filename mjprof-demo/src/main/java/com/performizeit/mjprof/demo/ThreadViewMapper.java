package com.performizeit.mjprof.demo;

import com.performizeit.mjprof.core.parser.ThreadInfo;

import java.util.Arrays;
import java.util.List;

final class ThreadViewMapper {

  private ThreadViewMapper() {
  }

  static ThreadView toView(ThreadInfo thread, int maxFrames) {
    return new ThreadView(thread.name(), thread.state(), thread.tid(), thread.daemon(),
        frames(thread, maxFrames));
  }

  /**
   * Renders a thread's stack into individual frame lines. mjprof models a stack as a Profile tree
   * whose toString is the rendered form, so splitting that is the honest way to get frames out.
   */
  static List<String> frames(ThreadInfo thread, int limit) {
    if (thread.stack() == null || limit <= 0) {
      return List.of();
    }
    return Arrays.stream(thread.stack().toString().split("\n"))
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .limit(limit)
        .toList();
  }
}
