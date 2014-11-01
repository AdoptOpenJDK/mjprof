package com.performizeit.mjprof.plugins.filters;

import com.performizeit.mjprof.plugin.types.Filter;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.util.ArrayList;

public abstract class SingleThreadFilter implements Filter, PipeHandler<ThreadDump, ThreadDump> {

    public ThreadDump map(ThreadDump jsd) {

        ThreadDump that = new ThreadDump();
        that.setHeader(jsd.getHeader());
        that.setJNIglobalReferences(jsd.getJNIglobalReferences());
        ArrayList<ThreadInfo> stcks = new ArrayList<ThreadInfo>();
        for (ThreadInfo stk : jsd.getStacks()) {
            if (filter(stk))
                stcks.add(stk);
        }
        that.setStacks(stcks);
        return that;


    }

    @Override
    abstract public boolean filter(ThreadInfo stck);

    @Override
    public ThreadDump handleMsg(ThreadDump msg) {
        return map(msg);
    }

    @Override
    public ThreadDump handleDone() {
        return null;
    }
}
