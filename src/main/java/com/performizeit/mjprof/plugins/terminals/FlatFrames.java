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

package com.performizeit.mjprof.plugins.terminals;

import com.performizeit.mjprof.api.Terminal;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.model.ProfileVisitor;
import com.performizeit.mjprof.model.SFNode;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;
import java.util.HashMap;
import static com.performizeit.mjprof.parser.ThreadInfoProps.*;

@SuppressWarnings("unused")
@Plugin(name="flat", params ={}, description="Shows flat histogram of the profiles")
public class FlatFrames implements Terminal,PipeHandler<ThreadDump,String> {
    HashMap<String,Integer> methods = new HashMap<String,Integer>();


    public void addStackDump(ThreadDump jsd) {
        for (ThreadInfo mss : jsd.getStacks()  ) {
            Profile p = (Profile) mss.getVal(STACK);
            p.visit(new ProfileVisitor() {
                @Override
                public void visit(SFNode stackframe, int level) {
                    if (stackframe.getNumChildren() ==0 && stackframe.getStackFrame() != null) {
                        Integer cnt = methods.get(stackframe.getStackFrame()) ;
                        if (cnt == null) {
                            cnt = 0;
                        }
                        cnt += stackframe.getCount();
                        methods.put(stackframe.getStackFrame(),cnt);
                    }
                }
            });
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key: methods.keySet()) {
            sb.append(String.format("%5d",methods.get(key))).append("  ").append(key).append("\n") ;

        }
        return sb.toString();
    }



    @Override public String handleMsg(ThreadDump msg) {
        addStackDump(msg);
        return null;}
    @Override public String handleDone() {
        return toString();
    }
}
