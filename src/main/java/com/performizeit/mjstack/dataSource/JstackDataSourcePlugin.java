package com.performizeit.mjstack.dataSource;

import com.performizeit.jmxsupport.JMXConnection;
import com.performizeit.mjstack.api.DataSource;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.monads.Param;
import com.performizeit.mjstack.parser.ThreadDump;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

// host:port or pid , freq,period       ,user,pass
@Plugin(name = "jstack", params = {@Param(value=int.class,name="pid"),
                                   @Param(value=int.class,name="frequency"),
                                    @Param(value = int.class,name="period")}, description = "Generate dumps using jstack")
public class JstackDataSourcePlugin implements DataSource {
    private final int freq;
    private final int period;
    int pid;

    public JstackDataSourcePlugin(int pid, int freq, int period) {
        this.pid = pid;
        this.freq = freq;
        this.period = period;
    }

    public ArrayList<ThreadDump> getThreadDumps() {
        ArrayList<ThreadDump> dumps = new ArrayList<ThreadDump>();
        try {


            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < period) {
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
                if (iterEnd - iterStart < freq)
                 Thread.sleep(freq - (iterEnd - iterStart));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dumps;
    }

}