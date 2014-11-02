package com.performizeit.mjprof.plugins.dataSource;

import com.performizeit.mjprof.plugin.types.DataSource;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.plumbing.GeneratorHandler;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.AttachNotSupportedException;
import sun.jvmstat.monitor.*;
import sun.tools.attach.HotSpotVirtualMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("unused")
@Plugin(name = "jstack", params = {@Param(type = String.class, value = "pid|mainClassName"),
        @Param(type = int.class, value = "count", optional = true, defaultValue = "1"),
        @Param(type = int.class, value = "sleep", optional = true, defaultValue = "1000")},
        description = "Generate dumps using jstack")
public class JstackDataSourcePlugin implements DataSource, GeneratorHandler<ThreadDump> {
    private final int count;
    private int iter =0;
    private final int sleep;
    private long lastIterTime=0;
    int pid;

    public JstackDataSourcePlugin(String pidStr, int count, int sleep) {
         try {
             this.pid = Integer.parseInt(pidStr);
         } catch (NumberFormatException e) {
             try {
                 pid = lookupProcessId(pidStr);
                 if (pid == -1) {
                     System.err.println("Process id for main class '"+pidStr + "' could not be resolved");
                     System.exit(1);
                 }
             } catch(Exception ne){
                 System.err.println("Process id for main class '"+pidStr + "' could not be resolved");
                 System.exit(1);
             }
         }

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
    public String runjStackCommandLine() throws IOException, InterruptedException {
        String[] commands = {System.getProperty("java.home") + "/../bin/jstack", Integer.toString(pid)};
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(commands);
        InputStream stdin = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdin);
        BufferedReader br = new BufferedReader(isr);
        StreamDataSourcePluginBase sds = new StreamDataSourcePluginBase() {};

        sds.setReader(br);
        int ret = proc.waitFor();
        if (ret !=0 ) {
            System.err.println("Executing jstack for process "+ pid + " failed");
        }
        return sds.getStackStringFromReader();

    }

    private ThreadDump getThreadDump() {
        long iterStart = System.currentTimeMillis();
        try {
         //   long start = System.currentTimeMillis();
            //     String str = runjStackCommandLine();
            String [] params = {"-;l"} ;
            String str = runThreadDump((new Integer(pid)).toString(),params);
           // System.err.println("tm ="+ (System.currentTimeMillis()-start));
            ThreadDump r;
            if (str == null) {
              r = null;
            } else r = new ThreadDump(str);

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



    private static String runThreadDump(String pid, String args[]) throws Exception {
        StringBuilder sb = new StringBuilder();
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(pid);
        } catch (Exception x) {
            String msg = x.getMessage();
            if (msg != null) {
                System.err.println(pid + ": " + msg);
            } else {
                x.printStackTrace();
            }
            if ((x instanceof AttachNotSupportedException) &&
                    (loadSAClass() != null)) {
                System.err.println("The -F option can be used when the target " +
                        "process is not responding");
            }
            System.exit(1);
        }


        // Cast to HotSpotVirtualMachine as this is implementation specific
        // method.
        InputStream in = ((HotSpotVirtualMachine)vm).remoteDataDump((Object[])args);

        // read to EOF and just print output

        byte b[] = new byte[256];
        int n;
        do {
            n = in.read(b);
            if (n > 0) {
                String s = new String(b, 0, n, "UTF-8");
                sb.append(s);

            }
        } while (n > 0);
        in.close();
        vm.detach();
        return sb.toString();
    }
    private static Class loadSAClass() {
        //
        // Attempt to load JStack class - we specify the system class
        // loader so as to cater for development environments where
        // this class is on the boot class path but sa-jdi.jar is on
        // the system class path. Once the JDK is deployed then both
        // tools.jar and sa-jdi.jar are on the system class path.
        //
        try {
            return Class.forName("sun.jvm.hotspot.tools.JStack", true,
                    ClassLoader.getSystemClassLoader());
        } catch (Exception x)  { }
        return null;
    }

    public static int lookupProcessId(String lookForMainClass) throws MonitorException, URISyntaxException {
        HostIdentifier hostId = new HostIdentifier("local://localhost");
        MonitoredHost monitoredHost =
                MonitoredHost.getMonitoredHost(hostId);

        // get the set active JVMs on the specified host.
        Set jvms = monitoredHost.activeVms();
        for (Iterator j = jvms.iterator(); j.hasNext(); /* empty */ ) {
            int lvmid = ((Integer) j.next()).intValue();
            MonitoredVm vm = null;
            String vmidString = "//" + lvmid + "?mode=r";
            VmIdentifier id = new VmIdentifier(vmidString);
            vm = monitoredHost.getMonitoredVm(id, 0);
            String mainClass = MonitoredVmUtil.mainClass(vm,
                    false);
            if (mainClass.equals(lookForMainClass)) return lvmid;
        }
        return -1;

    }

    public static void main(String[] args) throws MonitorException, URISyntaxException {
        System.out.println((lookupProcessId("My4tApp")));
    }

}