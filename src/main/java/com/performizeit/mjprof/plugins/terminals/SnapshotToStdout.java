package com.performizeit.mjprof.plugins.terminals;

import com.performizeit.mjprof.api.Outputer;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.plumbing.PipeHandler;

@SuppressWarnings("unused")
@Plugin(name="stdout", params ={}, description="Write to stdout")
public class SnapshotToStdout extends SnapshotToPrintStream {

    public SnapshotToStdout() {
        super(null);
    }
}
