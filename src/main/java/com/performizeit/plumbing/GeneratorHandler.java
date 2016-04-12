package com.performizeit.plumbing;

/**
 * Created by life on 22/8/14.
 */
public interface GeneratorHandler<E> {
  E generate();
  boolean isDone();
  void sleepBetweenIteration();
}
