package com.performizeit.plumbing;

/**
 * Created by life on 22/8/14.
 */
public interface PipeHandler<E> {
    E handleMsg(E msg);
    E handleDone();
}
