package com.performizeit.mjprof.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Populates this process with threads in recognizable states, so the endpoints have something
 * interesting to report. All threads are daemons and all are named with a demo- prefix.
 *
 * <p>ponytail: busy threads burn CPU in a tight loop rather than doing useful work — fine for a
 * demo, and it makes /api/threads/hot show a clear winner. Replace with real work if this ever
 * becomes more than a sample.
 */
@Component
public class DemoWorkload {

  private final int busyThreads;
  private final List<Thread> threads = new ArrayList<>();
  private final Object contendedLock = new Object();
  private final CountDownLatch neverCountedDown = new CountDownLatch(1);
  private volatile boolean running = true;

  public DemoWorkload(@Value("${demo.busy-threads:2}") int busyThreads) {
    this.busyThreads = busyThreads;
  }

  @PostConstruct
  void start() {
    for (int i = 1; i <= busyThreads; i++) {
      start("demo-busy-" + i, this::burnCpu);
    }
    // one thread holds the lock and sleeps; two more pile up BLOCKED behind it
    start("demo-lock-holder", this::holdLock);
    for (int i = 1; i <= 2; i++) {
      start("demo-blocked-" + i, this::waitForLock);
    }
    start("demo-waiting-1", this::waitForever);
  }

  private void start(String name, Runnable body) {
    Thread t = new Thread(body, name);
    t.setDaemon(true);
    t.start();
    threads.add(t);
  }

  private void burnCpu() {
    long sink = 0;
    while (running) {
      for (int i = 0; i < 1_000_000; i++) {
        sink += i * 31L;
      }
      if (sink == Long.MIN_VALUE) {
        return;   // never true; keeps the JIT from eliminating the loop
      }
    }
  }

  private void holdLock() {
    while (running) {
      synchronized (contendedLock) {
        sleep(500);
      }
      sleep(5);
    }
  }

  private void waitForLock() {
    while (running) {
      synchronized (contendedLock) {
        sleep(1);
      }
    }
  }

  private void waitForever() {
    try {
      neverCountedDown.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      running = false;
    }
  }

  @PreDestroy
  void stop() {
    running = false;
    threads.forEach(Thread::interrupt);
  }
}
