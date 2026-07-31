package com.performizeit.mjprof.demo;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.pipeline.Pipeline;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Each endpoint demonstrates one capability of the mjprof-core API, applied to this very process.
 * Every call takes a fresh snapshot on the request thread.
 */
@RestController
@RequestMapping("/api/threads")
public class ThreadEndpoints {

  /** Every live thread, with the top frames of its stack. */
  @GetMapping
  public List<ThreadView> threads(@RequestParam(defaultValue = "3") int frames) {
    return Pipeline.fromSelf().threads().stream()
        .map(t -> ThreadViewMapper.toView(t, frames))
        .toList();
  }

  /** Thread-state histogram, built with the API's group() rather than by counting in Java. */
  @GetMapping("/states")
  public List<GroupCount> states() {
    return Pipeline.fromSelf()
        .group(Attr.STATE)
        .threads().stream()
        .map(t -> new GroupCount(t.state(), t.count()))
        .sorted((a, b) -> Integer.compare(b.threads(), a.threads()))
        .toList();
  }

  /** Threads in one state, e.g. /api/threads/state/BLOCKED — the API's contains() filter. */
  @GetMapping("/state/{state}")
  public List<ThreadView> byState(@PathVariable String state,
                                  @RequestParam(defaultValue = "5") int frames) {
    String wanted = state.toUpperCase(Locale.ROOT);
    try {
      Thread.State.valueOf(wanted);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "unknown thread state '" + state + "'; expected one of "
              + Arrays.toString(Thread.State.values()));
    }
    return Pipeline.fromSelf()
        .contains(Attr.STATE, wanted)
        .threads().stream()
        .map(t -> ThreadViewMapper.toView(t, frames))
        .toList();
  }

  /**
   * Thread pools by size: strips the numeric suffix from thread names so pool-1-thread-{1,2,3}
   * collapse into one entry, then groups on the name.
   */
  @GetMapping("/pools")
  public List<GroupCount> pools() {
    return Pipeline.fromSelf()
        .withoutNameSuffix()
        .group(Attr.NAME)
        .threads().stream()
        .map(t -> new GroupCount(t.name(), t.count()))
        .sorted((a, b) -> Integer.compare(b.threads(), a.threads()))
        .toList();
  }

  /** Threads whose stack mentions the given text, trimmed to the frames around it. */
  @GetMapping("/search")
  public List<ThreadView> search(@RequestParam String frame,
                                 @RequestParam(defaultValue = "10") int frames) {
    if (frame == null || frame.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "frame must not be blank");
    }
    return Pipeline.fromSelf()
        .contains(Attr.STACK, frame)
        .threads().stream()
        .map(t -> ThreadViewMapper.toView(t, frames))
        .toList();
  }
}
