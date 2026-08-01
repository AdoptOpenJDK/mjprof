package com.performizeit.mjprof.demo;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.pipeline.Pipeline;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  private static final int MAX_SAMPLES = 50;
  private static final int MAX_SLEEP_MS = 2_000;

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
    return Pipeline.fromSelf()
        .contains(Attr.STATE, validState(state))
        .threads().stream()
        .map(t -> ThreadViewMapper.toView(t, frames))
        .toList();
  }

  /** Uppercases and validates a thread-state name, rejecting anything not in {@link Thread.State}. */
  private static String validState(String raw) {
    String wanted = raw.toUpperCase(Locale.ROOT);
    try {
      Thread.State.valueOf(wanted);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "unknown thread state '" + raw + "'; expected one of "
              + Arrays.toString(Thread.State.values()));
    }
    return wanted;
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

  /**
   * Sampled CPU profile of this process: takes several CPU-aware snapshots, merges them per thread
   * id so cpu_ns sums and stacks fold into a tree, then ranks by CPU.
   *
   * <p>Blocks the request thread for roughly samples * sleepMs; worst case is
   * (MAX_SAMPLES - 1) * MAX_SLEEP_MS ~= 98s (no sleep after the final sample), and each
   * concurrent caller occupies its own request thread for that long — these bounds are a demo-grade
   * guard against a single abusive request, not a production rate limit or a concurrency limit.
   */
  @GetMapping("/hot")
  public List<HotThread> hot(@RequestParam(defaultValue = "5") int samples,
                             @RequestParam(defaultValue = "100") int sleepMs,
                             @RequestParam(defaultValue = "10") int top,
                             @RequestParam(defaultValue = "5") int frames) {
    if (samples < 1 || samples > MAX_SAMPLES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "samples must be between 1 and " + MAX_SAMPLES + ", got " + samples);
    }
    if (sleepMs < 0 || sleepMs > MAX_SLEEP_MS) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "sleepMs must be between 0 and " + MAX_SLEEP_MS + ", got " + sleepMs);
    }
    if (top < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "top must be at least 1");
    }
    return Pipeline.fromSelfWithCpu(samples, sleepMs)
        .merge(Attr.TID)
        .sortDesc(Attr.CPU_NS)
        .threads().stream()
        .limit(top)
        .map(t -> new HotThread(t.name(), t.tid(), t.cpuNs(), t.count(),
            ThreadViewMapper.frames(t, frames)))
        .toList();
  }

  /**
   * All stacks of this process merged into one call tree, as text — the API's treeText() terminal,
   * which is what the CLI's {@code tree} monad prints.
   */
  @GetMapping(value = "/tree", produces = MediaType.TEXT_PLAIN_VALUE)
  public String tree(@RequestParam(required = false) String state) {
    Pipeline pipeline = Pipeline.fromSelf();
    if (state != null && !state.isBlank()) {
      pipeline = pipeline.contains(Attr.STATE, validState(state));
    }
    return pipeline.treeText();
  }
}
