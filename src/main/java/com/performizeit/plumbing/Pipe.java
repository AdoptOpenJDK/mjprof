package com.performizeit.plumbing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * pipe is a class which runs continuesly recieves msg from producers and trsfers them on after processing .
 */
public class Pipe<S,D> extends Thread   {
    AtomicInteger numProducers=new AtomicInteger(0);

    BlockingQueue<S> inQueue = new LinkedBlockingQueue<S>();
    private final PipeHandler<S,D> handler;
    private  Pipe<D,?> outgoingPipe =null;

    public Pipe(String name, PipeHandler<S,D> handler) {
        super() ;

        setName(name);
        this.handler = handler;
    }

    public void send(S e) {
        inQueue.add(e);

    }

    public void setOutgoingPipe(Pipe<D,?> outgoingPipe) {
        this.outgoingPipe = outgoingPipe;
        outgoingPipe.registerProducer();
    }

    public int registerProducer() {
        return numProducers.incrementAndGet();
    }
    public int producerDone() {
        int numP = numProducers.decrementAndGet();
        if (numP<=0) {
            interrupt();


        }
        return numP;
    }

    private void handleMsg(S e) {
        D e1 = handler.handleMsg(e);
        if (outgoingPipe != null && e1 != null) {
            outgoingPipe.send(e1);
        }
    }
    @Override
    public void run() {
        while (numProducers.get() > 0 ) {
            try {
                S a = inQueue.take();
                handleMsg(a);

            } catch (InterruptedException e) {
                if(numProducers.get() == 0) break;
            }
        }
        // it is stopped     get all remaining messages
        while (true) {
            S a = inQueue.poll();
            if (a ==  null) break;
            handleMsg(a);
        }
        D e = handler.handleDone();
        if (outgoingPipe != null) {
            if (e != null) outgoingPipe.send(e);
            outgoingPipe.producerDone();
        }
    }
}
