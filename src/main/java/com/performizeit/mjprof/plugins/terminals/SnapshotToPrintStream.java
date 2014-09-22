package com.performizeit.mjprof.plugins.terminals;

import com.performizeit.mjprof.plugin.types.Outputer;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.plumbing.PipeHandler;

import java.io.*;

@SuppressWarnings("unused")
@Plugin(name="snapshot", params ={ @Param(value="filename",optional=true)}, description="Write to a file")
public class SnapshotToPrintStream implements Outputer,PipeHandler {
    private final String fileName;
    private final PrintStream os;

    public SnapshotToPrintStream(String fileName) {
        this.fileName = fileName;
        if (fileName == null || fileName.length() == 0)  {
            os = System.out;
        } else {
            try {
                os = new PrintStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Override public Object handleMsg(Object msg) {
        os.println(msg.toString());
        os.flush();
        return msg;
    }
    @Override public Object handleDone() {
        os.close();
        return null;
    }
}
