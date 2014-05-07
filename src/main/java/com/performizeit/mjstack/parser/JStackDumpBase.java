package com.performizeit.mjstack.parser;

import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.model.JStackHeader;

import java.util.Comparator;

/**
 * Created by life on 30/3/14.
 */
public abstract class JStackDumpBase {
    protected JStackHeader header;
    protected JStackDumpBase() {

    }

    public abstract JStackDumpBase filterDump(JStackFilter filter) ;
    public abstract JStackDumpBase mapDump(JStackMapper mapper);
    public abstract JStackDumpBase sortDump(Comparator<ThreadInfo> comp);
    public abstract JStackDumpBase terminateDump(JStackTerminal terminal);

    public JStackHeader getHeader() {
        return header;
    }
}

