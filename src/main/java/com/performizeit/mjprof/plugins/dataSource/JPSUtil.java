package com.performizeit.mjprof.plugins.dataSource;

import sun.jvmstat.monitor.*;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by life on 2/11/14.
 */
public class JPSUtil {
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
}
