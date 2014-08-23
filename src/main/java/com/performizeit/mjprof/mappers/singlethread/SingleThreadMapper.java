package com.performizeit.mjprof.mappers.singlethread;

import com.performizeit.mjprof.api.DumpMapper;
import com.performizeit.mjprof.api.Mapper;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.ArrayList;

/**
 * Created by life on 22/8/14.
 */
public abstract class SingleThreadMapper implements Mapper,DumpMapper  {



    @Override
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
}
