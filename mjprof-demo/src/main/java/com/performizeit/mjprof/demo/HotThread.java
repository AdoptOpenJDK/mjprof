package com.performizeit.mjprof.demo;

import java.util.List;

/**
 * One thread in a sampled CPU profile. {@code samples} is how many snapshots the thread appeared in,
 * which merging on the thread id accumulates as the count.
 */
public record HotThread(String name, String tid, Long cpuNs, int samples, List<String> topFrames) {
}
