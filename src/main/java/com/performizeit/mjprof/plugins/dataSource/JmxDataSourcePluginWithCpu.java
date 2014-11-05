package com.performizeit.mjprof.plugins.dataSource;

import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.plugin.types.DataSource;
import com.performizeit.plumbing.GeneratorHandler;

import javax.management.openmbean.CompositeData;
import java.util.Date;
import java.util.HashMap;

import static com.performizeit.mjprof.parser.ThreadInfoProps.*;

// host:port or pid , freq,period       ,user,pass
@SuppressWarnings("unused")
@Plugin(name = "jmxc", params = {@Param("host:port|MainClass|pid"),
        @Param(type = int.class,value = "count",optional=true,defaultValue = "1"),
        @Param(type = int.class,value="sleep",optional=true,defaultValue = "5000"),
        @Param(value="username",optional=true),
        @Param(value="passwd",optional=true)
        }, description = "Generates thread dumps dumps via JMX and collects per thread CPU")
public class JmxDataSourcePluginWithCpu extends JmxDataSourcePlugin implements DataSource, GeneratorHandler<ThreadDump> {
    protected boolean collectCPU = true;

    public JmxDataSourcePluginWithCpu(String hostPortPid, int count, int sleep, String user, String pass) {
        super(hostPortPid,count,sleep,user,pass);
    }
    private double percentDouble(long nom,long denom) {
        return ((double)(nom*100*100/denom))/ 100.0;

    }
    @Override
    protected ThreadDump getThreadDump() throws Exception {
        ThreadDump threadDump = new ThreadDump();
        long iterStart = System.currentTimeMillis();
        try {

            threadDump.setHeader((new Date()).toString() + "\nThread dump via JMX of process " + hostPortPid);
            long[] threadsIds = server.getThreadIds();
            long pre=0;
            long post=0;
            long[] threadCPUpre = null;
            long[] threadCPUpost = null;
            if (collectCPU) {
                 pre = System.currentTimeMillis();
                threadCPUpre = server.getThreadsCPU(threadsIds);
            }
            CompositeData[] threads = server.getThreads(threadsIds, 10000);
            if (collectCPU) {
                lastIterTime = System.currentTimeMillis() - iterStart;
                try {
                    long toSleep     = sleep - lastIterTime;
                    if (toSleep >0)
                        Thread.sleep(toSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                post = System.currentTimeMillis();
                threadCPUpost = server.getThreadsCPU(threadsIds);

            }

            for (int thrdidx = 0; thrdidx < threadsIds.length; thrdidx++) {
                CompositeData thread = threads[thrdidx];

                HashMap<String, Object> props = getProps(thread);
                if (collectCPU) {
                    props.put(CPUNS, threadCPUpost[thrdidx] - threadCPUpre[thrdidx]);
                    props.put(WALL, post - pre);
                    props.put(CPU_PREC, percentDouble(threadCPUpost[thrdidx]- threadCPUpre[thrdidx],(post -pre)*1000*1000));
                }
                ThreadInfo threadInfo = new ThreadInfo(props);

                threadDump.addThreadInfo(threadInfo);


            }
        } finally {

            lastIterTime = System.currentTimeMillis() - iterStart;
            iter++;
        }


        return threadDump;
    }

    @Override
    public void sleepBetweenIteration() {
        return;
    }

}