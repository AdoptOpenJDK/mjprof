package com.performizeit.plumbing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * pipe is a class which runs continuesly recieves msg from producers and trsfers them on after processing .
 */
public class Pipe<E> extends Thread   {
    boolean producersDone = false;
    AtomicInteger numProducers=new AtomicInteger(0);

    BlockingQueue<E> queue = new LinkedBlockingQueue<E>();
    Thread consumer;
    private final PipeHandler<E> handler;
    private final Pipe<E> nextPipe;

    public Pipe(String name, Thread consumer, PipeHandler<E> handler, Pipe<E> nextPipe) {
        super() ;
        this.nextPipe = nextPipe;
        setName(name);
        this.consumer = consumer;
        this.handler = handler;
    }

    public void send(E e) {
        queue.add(e);

    }
    public int registerProducer() {
        return numProducers.incrementAndGet();
    }
    public int producerDone() {
        int numP = numProducers.decrementAndGet();
        if (numP<=0) {
            consumer.interrupt();


        }
        return numP;
    }

    private void handleMsg(E e) {
        E e1 = handler.handleMsg(e);
        if (nextPipe != null && e1 != null) {
            nextPipe.send(e1);
        }
    }
    @Override
    public void run() {
        while (numProducers.get() > 0 ) {
            try {
                E a = queue.take();
                handleMsg(a);

            } catch (InterruptedException e) {
                if(numProducers.get() == 0) break;
            }
        }
        // it is stopped     get all remaining messages
        while (true) {
            E a = queue.poll();
            if (a ==  null) break;
            handleMsg(a);
        }
        E e = handler.handleDone();
        if (nextPipe != null) {
            if (e != null) nextPipe.send(e);
            nextPipe.producerDone();
        }
    }
}
