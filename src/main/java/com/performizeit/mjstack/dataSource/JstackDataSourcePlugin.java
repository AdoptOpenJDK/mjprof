package com.performizeit.mjstack.dataSource;

import com.performizeit.jmxsupport.JMXConnection;
import com.performizeit.mjstack.api.DataSource;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadDump;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

// host:port or pid , freq,period       ,user,pass
@Plugin(name = "jstack", paramTypes = {int.class, int.class, int.class}, description = "Generate dumps via JMX")
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
        JMXConnection server=null;
        ArrayList<ThreadDump> dumps = new ArrayList<ThreadDump>();
        try {


            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < period) {

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
                Thread.sleep(freq);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dumps;
    }

}