package com.performizeit.mjprof.filters;

import com.performizeit.mjprof.api.DumpMapper;
import com.performizeit.mjprof.api.Filter;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.ArrayList;

/**
 * Created by life on 22/8/14.
 */
public abstract class SingleThreadFilter implements Filter,DumpMapper {



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
}
