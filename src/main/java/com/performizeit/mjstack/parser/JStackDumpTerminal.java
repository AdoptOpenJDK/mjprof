package com.performizeit.mjstack.parser;

import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.JStackTerminal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Comparator;

/**
 * Created by life on 30/3/14.
 */
public class JStackDumpTerminal extends JStackDumpBase{
    protected  String data;
    public JStackDump filterDump(JStackFilter filter) {
        throw new NotImplementedException();
    }
    public JStackDump mapDump(JStackMapper mapper) {
        throw new NotImplementedException();
    }
    public JStackDump sortDump(Comparator<ThreadInfo> comp) {
        throw new NotImplementedException();
    }

    @Override
    public JStackDumpBase terminateDump(JStackTerminal terminal) {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return header+"\n\n"+data;
    }
}
