package com.performizeit.mjprof.core.plumbing;

public interface PipeHandler<S, D> {
  D handleMsg(S msg);
  D handleDone();
}
