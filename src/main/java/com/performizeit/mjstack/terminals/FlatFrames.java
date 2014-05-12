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

package com.performizeit.mjstack.terminals;

import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileVisitor;
import com.performizeit.mjstack.model.SFNode;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import java.util.HashMap;
import static com.performizeit.mjstack.parser.ThreadInfoProps.*;


@Plugin(name="flat", paramTypes={}, description="Shows flat histogram of the profiles")
public class FlatFrames implements JStackTerminal {
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
}
