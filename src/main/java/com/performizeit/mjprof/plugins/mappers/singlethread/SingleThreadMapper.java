package com.performizeit.mjprof.plugins.mappers.singlethread;


import com.performizeit.mjprof.plugin.types.Mapper;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.util.ArrayList;


public abstract class SingleThreadMapper implements Mapper ,PipeHandler<ThreadDump,ThreadDump> {
    public ThreadDump map(ThreadDump jsd) {

        ThreadDump that = new ThreadDump();
        that.setHeader(jsd.getHeader());
            ArrayList<ThreadInfo> stcks = new ArrayList<ThreadInfo>();
            for (ThreadInfo stk: jsd.getStacks()) {
                stcks.add(map(stk));
            }
            that.setStacks(stcks);
            return that;

    }

    abstract public ThreadInfo map(ThreadInfo stck);
    @Override public ThreadDump handleMsg(ThreadDump msg) { return map(msg);}
    @Override public ThreadDump handleDone() {return null;}
}
