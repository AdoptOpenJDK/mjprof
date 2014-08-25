package com.performizeit.mjprof.plugins.terminals;

import com.performizeit.mjprof.api.Plugin;

@SuppressWarnings("unused")
@Plugin(name="stdout", params ={}, description="Write to stdout")
public class SnapshotToStdout extends SnapshotToPrintStream {

    public SnapshotToStdout() {
        super(null);
    }
}
