package com.performizeit.mjstack.dataSource;

import com.performizeit.jmxsupport.JMXConnection;
import com.performizeit.mjstack.api.DataSource;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.api.Param;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import javax.management.openmbean.CompositeData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import static com.performizeit.mjstack.parser.ThreadInfoProps.*;

// host:port or pid , freq,period       ,user,pass
@Plugin(name = "jmx", params = {@Param("host:port|pid"),

            @Param(type = int.class,value="frequency"),
            @Param(type = int.class,value = "period"),
            @Param(type = boolean.class,value="collect-cpu",optional=true,defaultValue = "false"),
            @Param(value="username",optional=true),
            @Param(value="passwd",optional=true)
            }, description = "Generate dumps via JMX ")
public class JmxDataSourcePlugin implements DataSource {
    private final int freq;
    private final int period;
    private final String user;
    private final String pass;
    boolean collectCPU = true;
    String hostPortPid;

    public JmxDataSourcePlugin(String hostPortPid, boolean collectCPU,int freq, int period, String user, String pass) {
        this.hostPortPid = hostPortPid;
        this.freq = freq;
        this.collectCPU = collectCPU;
        this.period = period;
        this.user = user.trim().isEmpty() ? null : user;;

        this.pass = pass.trim().isEmpty() ? null : pass;



    }

    public ArrayList<ThreadDump> getThreadDumps() {
        JMXConnection server=null;
        ArrayList<ThreadDump> dumps = new ArrayList<ThreadDump>();
        try {
            try {
                Integer.parseInt(hostPortPid);
                server = new JMXConnection(hostPortPid);
            } catch (NumberFormatException e) {

                server = new JMXConnection(hostPortPid, user, pass);
            }
            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start < period) {
                long iterStart = System.currentTimeMillis();
                ThreadDump threadDump = new ThreadDump();
                threadDump.setHeader((new Date()).toString() +  "\nThread dump via JMX of process "+ hostPortPid);
                long[] threadsIds = server.getThreadIds();
                CompositeData[] threads = server.getThreads(threadsIds, 10000);
                long[] threadCPUs = null;
                if (collectCPU) {
                    threadCPUs = server.getThreadsCPU(threadsIds);
                }
                for(int thrdidx=0;thrdidx< threadsIds.length;thrdidx++) {
                    CompositeData thread = threads[thrdidx];

                    HashMap<String,Object> props = new HashMap<String,Object>();
                    props.put(NAME,thread.get("threadName"));
                    props.put(TID,thread.get("threadId"));
                    props.put(STATE,thread.get("threadState"));
                    if (collectCPU) {
                        props.put(CPU, threadCPUs[thrdidx]);
                    }

                    CompositeData[] stec = (CompositeData[]) thread.get("stackTrace");
                    StackTraceElement[] ste = new StackTraceElement[stec.length];
                    for(int i=0;i<stec.length;i++) {
                        ste[i] = new StackTraceElement((String)stec[i].get("className"),(String)stec[i].get("methodName"),
                                (String)stec[i].get("fileName"),(Integer)stec[i].get("lineNumber"));
                    }
                    Profile p = new Profile();
                    p.addSingle(ste);
                    props.put(STACK,p);
                    ThreadInfo threadInfo = new ThreadInfo(props);

                    threadDump.addThreadInfo(threadInfo);

                }
                dumps.add(threadDump);
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