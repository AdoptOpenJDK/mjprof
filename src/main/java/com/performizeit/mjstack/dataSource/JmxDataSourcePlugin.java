package com.performizeit.mjstack.dataSource;

import com.performizeit.jmxsupport.JMXConnection;
import com.performizeit.mjstack.api.DataSource;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import javax.management.openmbean.CompositeData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import static com.performizeit.mjstack.parser.ThreadInfoProps.*;

// host:port or pid , freq,period       ,user,pass
@Plugin(name = "jmx", paramTypes = {String.class, int.class, int.class, String.class, String.class}, description = "Generate dumps via JMX host:port,frequency,period,[user],[passwd]")
public class JmxDataSourcePlugin implements DataSource {
    private final int freq;
    private final int period;
    private final String user;
    private final String pass;
    String hostPortPid;

    public JmxDataSourcePlugin(String hostPortPid, int freq, int period, String user, String pass) {
        this.hostPortPid = hostPortPid;
        this.freq = freq;
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
                ThreadDump threadDump = new ThreadDump();
                threadDump.setHeader((new Date()).toString() +  "\nThread dump via JMX of process "+ hostPortPid);
                long[] threadsIds = server.getThreadIds();
                CompositeData[] threads = server.getThreads(threadsIds, 10000);
                for (CompositeData thread: threads) {
                    HashMap<String,Object> props = new HashMap<String,Object>();
                    props.put(NAME,thread.get("threadName"));
                    props.put(TID,thread.get("threadId"));
                    props.put(STATE,thread.get("threadState"));

                    CompositeData[] stec = (CompositeData[]) thread.get("stackTrace");
                    StackTraceElement[] ste = new StackTraceElement[stec.length];
                    for(int i=0;i<stec.length;i++) {
                        ste[i] = new StackTraceElement((String)stec[i].get("className"),(String)stec[i].get("methodName"),
                                (String)stec[i].get("fileName"),(Integer)stec[i].get("lineNumber"));
                    }
                    Profile p = new Profile();
                    p.addSingle(ste);
                    props.put(STACK,p);
                    ThreadInfo ti = new ThreadInfo(props);

                    threadDump.addThreadInfo(ti);

                }
                dumps.add(threadDump);
                if (System.currentTimeMillis() + freq - start >= period ) break;
                Thread.sleep(freq);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dumps;
    }

}