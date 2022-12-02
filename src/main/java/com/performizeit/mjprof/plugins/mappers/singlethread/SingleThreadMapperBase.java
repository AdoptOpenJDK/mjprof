package com.performizeit.mjprof.plugins.mappers.singlethread;


import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.util.ArrayList;


public abstract class SingleThreadMapperBase implements com.performizeit.mjprof.plugin.types.SingleThreadMapper, PipeHandler<ThreadDump, ThreadDump> {

  public ThreadDump map(ThreadDump jsd) {
    ThreadDump that = new ThreadDump();
    that.setHeader(jsd.getHeader());
    that.setJNIglobalReferences(jsd.getJNIglobalReferences());
    ArrayList<ThreadInfo> stcks = new ArrayList<>();
    for (ThreadInfo stk : jsd.getThreadInfos()) {
      stcks.add(map(stk));
    }
    that.setThreadInfos(stcks);
    return that;

  }

  abstract public ThreadInfo map(ThreadInfo stck);

  @Override
  public ThreadDump handleMsg(ThreadDump msg) {
    return map(msg);
  }

  @Override
  public ThreadDump handleDone() {
    return null;
  }
}
