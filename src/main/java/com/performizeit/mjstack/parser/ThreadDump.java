/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjstack.parser;

import com.performizeit.mjstack.api.DumpMapper;
import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.model.JStackHeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ThreadDump  {
    protected JStackHeader header;
    public JStackHeader getHeader() {
        return header;
    }

    int JNIglobalReferences = -1;
    public static String JNI_GLOBAL_REFS= "JNI global references:";
    ArrayList<ThreadInfo> stacks;
    public ThreadDump(String stringRep) {
        String[] splitTraces = stringRep.split("\n\"");  // Assuming that thread stack trace starts with a new line followed by "

        header = new JStackHeader(splitTraces[0]);
        stacks = new ArrayList<ThreadInfo>();
        for(int i=1;i<splitTraces.length;i++) {
            if (splitTraces[i].startsWith(JNI_GLOBAL_REFS)) {
                try {
                    JNIglobalReferences = Integer.parseInt(splitTraces[i].substring(splitTraces[i].indexOf(":") + 2).trim());
                } catch (NumberFormatException e) {
                    // do nothing so we missed the JNI global references I do not know what to do with it.
                }

            } else {
                stacks.add(new ThreadInfo("\"" + splitTraces[i]));
            }
        }
    }
    public  ThreadDump() {
      super();
    }

    public ArrayList<ThreadInfo> getStacks() {
        return stacks;
    }
    public void addThreadInfo(ThreadInfo ti) {
        stacks.add(ti);
    }

    public void  setStacks(ArrayList<ThreadInfo> stacks) {
        this.stacks = stacks;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(header+"\n\n");

        for (ThreadInfo stack : stacks) {
            s.append(stack.toString()+"\n");
        }

        return s.toString();
    }
    public ThreadDump filterDump(JStackFilter filter) {
        ThreadDump that = new ThreadDump();
        that.header = header;
        that.stacks = new ArrayList<ThreadInfo>();
        for (ThreadInfo stk: stacks) {
            if (filter.filter(stk)) {
                that.stacks.add(stk);
            }
        }
        return that;
    }
    public ThreadDump mapDump(JStackMapper mapper) {
        ThreadDump that = new ThreadDump();
        that.header = header;
        that.stacks = new ArrayList<ThreadInfo>();
        for (ThreadInfo stk: stacks) {
            that.stacks.add(mapper.map(stk));
        }
        return that;
    }
    public ThreadDump mapDump(DumpMapper mapper) {

        return mapper.map(this);
    }
    public ThreadDump sortDump(Comparator<ThreadInfo> comp) {
        ThreadDump that = new ThreadDump();
        that.header = header;
        that.stacks = new ArrayList<ThreadInfo>();
        for (ThreadInfo stk: stacks) {
            that.stacks.add(stk);
        }
        Collections.sort(that.stacks,comp);
        return that;
    }

    public ThreadDump terminateDump(JStackTerminal terminal) {
        JStackDumpTerminal that = new JStackDumpTerminal();
        that.header = header;
       terminal.addStackDump(this);
        that.data = terminal.toString();
        return that;
    }


    public void setHeader(String header) {
        this.header = new JStackHeader(header);
    }
}
