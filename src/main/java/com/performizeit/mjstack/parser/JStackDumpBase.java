package com.performizeit.mjstack.parser;

import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.JStackTerminal;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
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
    public abstract JStackDumpBase sortDump(Comparator<JStackMetadataStack> comp);
    public abstract JStackDumpBase terminateDump(JStackTerminal terminal);

    public JStackHeader getHeader() {
        return header;
    }
}

