package com.performizeit.plumbing;

/**
 * Created by life on 22/8/14.
 */
public class Generator<E> extends Thread {

  private final GeneratorHandler<E> generator;
  private final Pipe<E, ?> pipe;

  public Generator(String name, GeneratorHandler<E> generator, Pipe<E, ?> pipe) {
    super(name);
    this.generator = generator;
    this.pipe = pipe;
    this.pipe.registerProducer();
  }

  @Override
  public void run() {
    while (!generator.isDone()) {
      E data = generator.generate();
      if (pipe != null && data != null) {
        pipe.send(data);
      }
      if (generator.isDone()) {
        this.pipe.producerDone();
      } else {
        generator.sleepBetweenIteration();
      }
    }
  }
}
