package com.performizeit.mjprof.demo;

import java.util.List;

/**
 * JSON view of one thread. mjprof's ThreadInfo is a heterogeneous property bag whose keys depend on
 * the data source, and its accessors are not bean-shaped, so it is never serialized directly — this
 * record is the stable wire contract.
 */
public record ThreadView(String name, String state, String tid, boolean daemon,
                         List<String> topFrames) {
}
