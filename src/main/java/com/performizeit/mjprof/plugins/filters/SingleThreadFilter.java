package com.performizeit.mjprof.plugins.filters;

import com.performizeit.mjprof.api.DumpMapper;
import com.performizeit.mjprof.api.Filter;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.util.ArrayList;

/**
 * Created by life on 22/8/14.
 */
public abstract class SingleThreadFilter implements Filter,DumpMapper,PipeHandler<ThreadDump,ThreadDump> {
    @Override
    public ThreadDump map(ThreadDump jsd) {

        ThreadDump that = new ThreadDump();
        that.setHeader(jsd.getHeader());
            ArrayList<ThreadInfo> stcks = new ArrayList<ThreadInfo>();
            for (ThreadInfo stk: jsd.getStacks()) {
                if (filter(stk))
                    stcks.add(stk);
            }
            that.setStacks(stcks);
            return that;


    }

    @Override
    abstract public boolean filter(ThreadInfo stck);
    @Override public ThreadDump handleMsg(ThreadDump msg) { return map(msg);}
    @Override public ThreadDump handleDone() {return null;}
}
