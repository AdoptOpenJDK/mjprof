package com.performizeit.mjstack.mergers;

import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.JStackProps;
import com.performizeit.mjstack.parser.StackTrace;
import com.performizeit.mjstack.parser.ThreadInfo;


@Plugin(name="tree", paramTypes={},description="combine all stack traces ")
public class StackTreeMerger implements JStackTerminal {
    StackTree st = new StackTree();
    public void addStackDump(JStackDump jsd) {

        for (ThreadInfo mss : jsd.getStacks()  ) {
            st.addStacktrace((StackTrace) mss.getVal(JStackProps.STACK));      // System.out.println();

        }
    }

    @Override
    public String toString() {
        return st.toString();
    }
}
