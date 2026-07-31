package com.performizeit.mjprof.core.pipeline;

import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.plugin.types.DataSource;
import com.performizeit.mjprof.core.plumbing.GeneratorHandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Feeds thread dumps a caller already holds into a pipeline.
 *
 * <p>Deliberately not annotated with @Plugin: an in-memory source is meaningless from the command
 * line, and it must not appear in supported_monads.txt.
 */
final class DumpsDataSource implements DataSource, GeneratorHandler<ThreadDump> {

  private final Iterator<ThreadDump> remaining;

  DumpsDataSource(Collection<ThreadDump> dumps) {
    this.remaining = List.copyOf(dumps).iterator();
  }

  @Override
  public ThreadDump generate() {
    return remaining.hasNext() ? remaining.next() : null;
  }

  @Override
  public boolean isDone() {
    return !remaining.hasNext();
  }

  @Override
  public void sleepBetweenIteration() {
  }
}
