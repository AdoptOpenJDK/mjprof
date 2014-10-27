package com.performizeit.mjprof.plugins.dataSource;

import com.performizeit.mjprof.plugin.types.DataSource;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.plumbing.GeneratorHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

@SuppressWarnings("unused")
@Plugin(name = "jstack", params = {@Param(type = int.class, value = "pid"),
        @Param(type = int.class, value = "count", optional = true, defaultValue = "1"),
        @Param(type = int.class, value = "sleep", optional = true, defaultValue = "1000")},
        description = "Generate dumps using jstack")
public class JstackDataSourcePlugin implements DataSource, GeneratorHandler<ThreadDump> {
    private final int count;
    private int iter =0;
    private final int sleep;
    private long lastIterTime=0;
    int pid;

    public JstackDataSourcePlugin(int pid, int count, int sleep) {
        this.pid = pid;
        this.count = count;
        this.sleep = sleep;
    }

    public ArrayList<ThreadDump> getThreadDumps() {
        ArrayList<ThreadDump> dumps = new ArrayList<ThreadDump>();
        try {
            for (iter = 0; iter < count; iter++) {
                long iterStart = System.currentTimeMillis();
                dumps.add(getThreadDump());
                long iterEnd = System.currentTimeMillis();
                if (iter < count - 1 && iterEnd - iterStart < sleep)
                    Thread.sleep(sleep - (iterEnd - iterStart));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dumps;
    }

    private ThreadDump getThreadDump() {
        long iterStart = System.currentTimeMillis();
        try {
            String[] commands = {System.getProperty("java.home") + "/../bin/jstack", Integer.toString(pid)};
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(commands);
            InputStream stdin = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            StreamDataSourcePluginBase sds = new StreamDataSourcePluginBase() {

            };

            sds.setReader(br);
            String str = sds.getStackStringFromReader();
            ThreadDump r;
            if (str == null) {
              r = null;
            } else r = new ThreadDump(str);
            int ret = proc.waitFor();
            if (ret !=0 ) {
                System.err.println("Executing jstack for process "+ pid + " failed");
            }
            iter ++;
            return r;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  finally {
            lastIterTime = System.currentTimeMillis() -iterStart;

        }

    }

    @Override
    public ThreadDump generate() {
        return getThreadDump();
    }

    @Override
    public boolean isDone() {
        return iter >=count;
    }

    @Override
    public void sleepBetweenIteration() {
        if ( lastIterTime < sleep)
            try {
                Thread.sleep(sleep - lastIterTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }
}