package com.performizeit.plumbing;

public interface PipeHandler<S,D> {
    D handleMsg(S msg);
    D handleDone();
}
