package com.performizeit.mjprof.demo;

import com.performizeit.mjprof.core.api.Attr;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.pipeline.Pipeline;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
