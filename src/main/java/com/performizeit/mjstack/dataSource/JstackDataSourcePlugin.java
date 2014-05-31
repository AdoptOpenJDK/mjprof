package com.performizeit.mjstack.dataSource;

import com.performizeit.mjstack.api.DataSource;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.api.Param;
import com.performizeit.mjstack.parser.ThreadDump;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

@Plugin(name = "jstack", params = {@Param(type=int.class,value="pid"),
                                   @Param(type=int.class,value="count",optional = true,defaultValue = "1"),
                                    @Param(type = int.class,value="sleep",optional = true,defaultValue = "1000")},
        description = "Generate dumps using jstack")
public class JstackDataSourcePlugin implements DataSource {
    private final int count;
    private final int sleep;
    int pid;

    public JstackDataSourcePlugin(int pid, int count, int sleep) {
        this.pid = pid;
        this.count = count;
        this.sleep = sleep;
    }

    public ArrayList<ThreadDump> getThreadDumps() {
        ArrayList<ThreadDump> dumps = new ArrayList<ThreadDump>();
        try {
           for(int iter =0;iter<count;iter++) {
                long iterStart = System.currentTimeMillis();
                String[] commands = {System.getProperty("java.home")+"/../bin/jstack",Integer.toString(pid)};
                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(commands);
                InputStream stdin = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);
                StreamDataSourcePluginBase sds = new StreamDataSourcePluginBase(){
                   @Override
                    public ArrayList<ThreadDump> getThreadDumps() {
                        return null;
                    }
                };
                dumps.add(sds.buildJstacks(sds.getStackStringsFromReader(br)).get(0));
                proc.waitFor();
                long iterEnd = System.currentTimeMillis();
                if (iter <count -1  && iterEnd - iterStart < sleep)
                 Thread.sleep(sleep - (iterEnd - iterStart));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dumps;
    }
}