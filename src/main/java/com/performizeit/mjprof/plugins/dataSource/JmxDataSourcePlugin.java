package com.performizeit.mjprof.plugins.dataSource;

import com.performizeit.jmxsupport.JMXConnection;
import com.performizeit.mjprof.api.PluginCategory;
import com.performizeit.mjprof.plugin.types.DataSource;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.GeneratorHandler;

import javax.management.openmbean.CompositeData;
import java.util.Date;
import java.util.HashMap;

import static com.performizeit.mjprof.parser.ThreadInfoProps.*;

// host:port or pid , freq,period       ,user,pass
@SuppressWarnings("unused")
@Plugin(name = "jmx", params = {@Param("host:port"),
  @Param(type = int.class, value = "count", optional = true, defaultValue = "1"),
  @Param(type = int.class, value = "sleep", optional = true, defaultValue = "5000"),
  @Param(value = "username", optional = true),
  @Param(value = "passwd", optional = true)
},
    category = PluginCategory.DATA_SOURCE,
    description = "Generates thread dumps via JMX ")
public class JmxDataSourcePlugin implements DataSource, GeneratorHandler<ThreadDump> {
  protected final int sleep;
  protected final int count;
  protected int iter = 0;

  protected String hostPort;
  protected JMXConnection server;
  protected long lastIterTime = 0;

  public JmxDataSourcePlugin(String hostPort, int count, int sleep, String user, String pass) {
    this.hostPort = hostPort;
    this.count = count;
    this.sleep = sleep;
    user = user.trim().isEmpty() ? null : user;
    pass = pass.trim().isEmpty() ? null : pass;
    try {
      server = new JMXConnection(hostPort, user, pass);
    } catch (Exception e) {
      System.err.println("ERROR: Unable to open JMX connection for" + hostPort);
      System.exit(1);
      server = null;

    }
  }


  protected ThreadDump getThreadDump() throws Exception {
    ThreadDump threadDump = new ThreadDump();
    long iterStart = System.currentTimeMillis();
    try {

      threadDump.setHeader((new Date()) + "\nThread dump via JMX of process " + hostPort);
      long[] threadsIds = server.getThreadIds();


      CompositeData[] threads = server.getThreads(threadsIds, 10000);

      for (int thrdidx = 0; thrdidx < threadsIds.length; thrdidx++) {
        CompositeData thread = threads[thrdidx];

        HashMap<String, Object> props = getProps(thread);
        ThreadInfo threadInfo = new ThreadInfo(props);

        threadDump.addThreadInfo(threadInfo);


      }
    } finally {

      lastIterTime = System.currentTimeMillis() - iterStart;
      iter++;
    }


    return threadDump;
  }

  protected HashMap<String, Object> getProps(CompositeData thread) {
    HashMap<String, Object> props = new HashMap<>();
    props.put(NAME, thread.get("threadName"));
    props.put(TID, thread.get("threadId"));
    props.put(STATE, thread.get("threadState"));


    CompositeData[] stec = (CompositeData[]) thread.get("stackTrace");
    StackTraceElement[] ste = new StackTraceElement[stec.length];
    for (int i = 0; i < stec.length; i++) {
      ste[i] = new StackTraceElement((String) stec[i].get("className"), (String) stec[i].get("methodName"),
        (String) stec[i].get("fileName"), (Integer) stec[i].get("lineNumber"));
    }
    Profile p = new Profile();
    p.addSingle(ste);
    props.put(STACK, p);
    return props;
  }

  @Override
  public ThreadDump generate() {
    try {
      return getThreadDump();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isDone() {
    return iter >= count;
  }

  @Override
  public void sleepBetweenIteration() {
    if (lastIterTime < sleep)
      try {
        Thread.sleep(sleep - lastIterTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

  }

}
