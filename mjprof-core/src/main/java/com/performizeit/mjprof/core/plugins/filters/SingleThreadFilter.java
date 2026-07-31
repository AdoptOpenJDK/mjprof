package com.performizeit.mjprof.core.plugins.filters;

import com.performizeit.mjprof.core.plugin.types.Filter;
import com.performizeit.mjprof.core.parser.ThreadDump;
import com.performizeit.mjprof.core.parser.ThreadInfo;
import com.performizeit.mjprof.core.plumbing.PipeHandler;

import java.util.ArrayList;

public abstract class SingleThreadFilter implements Filter, PipeHandler<ThreadDump, ThreadDump> {

  public ThreadDump map(ThreadDump jsd) {

    ThreadDump that = new ThreadDump();
    that.setHeader(jsd.getHeader());
    that.setJNIglobalReferences(jsd.getJNIglobalReferences());
    ArrayList<ThreadInfo> stcks = new ArrayList<>();
    for (ThreadInfo stk : jsd.getThreadInfos()) {
      if (filter(stk))
        stcks.add(stk);
    }
    that.setThreadInfos(stcks);
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
