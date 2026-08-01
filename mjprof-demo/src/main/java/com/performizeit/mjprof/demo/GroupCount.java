package com.performizeit.mjprof.demo;

/** One bucket of a grouped dump: the grouping key and how many threads collapsed into it. */
public record GroupCount(String key, int threads) {
}
