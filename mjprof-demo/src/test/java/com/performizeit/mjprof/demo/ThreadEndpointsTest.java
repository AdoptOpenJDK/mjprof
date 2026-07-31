package com.performizeit.mjprof.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Assertions check structure, never counts or timings — the subject under test is a live JVM whose
 * thread set changes between requests.
 */
@WebMvcTest(ThreadEndpoints.class)
class ThreadEndpointsTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  void threadsEndpointListsThisProcessThreads() throws Exception {
    mockMvc.perform(get("/api/threads"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").exists())
        .andExpect(jsonPath("$[0].state").exists())
        .andExpect(jsonPath("$[0].tid").exists());
  }

  @Test
  void threadsEndpointHonoursTheFrameLimit() throws Exception {
    mockMvc.perform(get("/api/threads").param("frames", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].topFrames.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(1)));
  }

  @Test
  void statesEndpointReturnsAHistogram() throws Exception {
    mockMvc.perform(get("/api/threads/states"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].key").exists())
        .andExpect(jsonPath("$[0].threads").exists());
  }
}
