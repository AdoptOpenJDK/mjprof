package com.performizeit.mjprof.comparators;

import com.performizeit.mjprof.api.DumpMapper;
import com.performizeit.mjprof.api.Mapper;
import com.performizeit.mjprof.api.ThreadInfoComparator;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by life on 23/8/14.
 */
public abstract class SortMapper  implements ThreadInfoComparator,DumpMapper {


    @Override
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
}
