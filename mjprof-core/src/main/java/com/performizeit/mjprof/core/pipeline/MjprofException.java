package com.performizeit.mjprof.core.pipeline;

/** Thrown for any failure surfaced through the Pipeline API. */
public class MjprofException extends RuntimeException {

  public MjprofException(String message) {
    super(message);
  }

  public MjprofException(String message, Throwable cause) {
    super(message, cause);
  }
}
