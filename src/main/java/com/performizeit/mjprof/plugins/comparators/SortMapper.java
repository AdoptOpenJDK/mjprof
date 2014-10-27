package com.performizeit.mjprof.plugins.comparators;

import com.performizeit.mjprof.api.ThreadInfoComparator;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.util.ArrayList;
import java.util.Collections;

public abstract class SortMapper  implements ThreadInfoComparator,PipeHandler<ThreadDump,ThreadDump> {
    public ThreadDump map(ThreadDump jsd) {
        ThreadDump that = new ThreadDump();
        that.setHeader(jsd.getHeader());
        ArrayList<ThreadInfo>stacks = jsd.cloneStacks();
        Collections.sort(stacks, this);
        that.setStacks(stacks);
        return that;

    }

    @Override
    abstract public int compare(ThreadInfo o1, ThreadInfo o2);
    @Override public ThreadDump handleMsg(ThreadDump msg) { return map(msg);}
    @Override public ThreadDump handleDone() {return null;}
}
