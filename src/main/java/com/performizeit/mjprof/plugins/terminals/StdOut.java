package com.performizeit.mjprof.plugins.terminals;

import com.performizeit.mjprof.api.Outputer;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.plumbing.PipeHandler;

@SuppressWarnings("unused")
@Plugin(name="stdout", params ={}, description="Write to stdout")
public class StdOut implements Outputer,PipeHandler {
    @Override public Object handleMsg(Object msg) {
        System.out.println(msg.toString());
        System.out.flush();
        return msg;}
    @Override public Object handleDone() {

        return null;
    }
}
