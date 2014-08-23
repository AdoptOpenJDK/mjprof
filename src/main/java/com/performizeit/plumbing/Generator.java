package com.performizeit.plumbing;

/**
 * Created by life on 22/8/14.
 */
public class Generator<E> extends Thread {

    private final GeneratorHandler<E> generator;
    private final Pipe<E> pipe;

    public Generator(String name, GeneratorHandler<E> generator,Pipe<E> pipe ) {
        super(name);
        this.generator = generator;
        this.pipe = pipe;
        this.pipe.registerProducer();
    }

    @Override
    public void run() {
        while (true) {
            E data = generator.  generate();
            if (data == null) {
                this.pipe.producerDone();
                break;
            }  else {
                pipe.send(data);
            }
        }
    }
}
