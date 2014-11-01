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

package com.performizeit.mjprof.plugins.mappers.singlethread.stackframe;


import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.model.ProfileVisitor;
import com.performizeit.mjprof.model.SFNode;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.plugins.mappers.singlethread.SingleThreadMapperBase;

import static com.performizeit.mjprof.parser.ThreadInfoProps.STACK;

@SuppressWarnings("unused")
@Plugin(name="atelim", params = {},
        description = "Eliminates package name from stack frames")
public class AtEliminator extends SingleThreadMapperBase {
    @Override
    public ThreadInfo map(ThreadInfo stck) {
        Profile jss = (Profile) stck.getVal(STACK);
        jss.visit(new ProfileVisitor() {
            @Override
            public void visit(SFNode sf,int level) {
                if (sf.getStackFrame() == null) return;
                sf.setStackFrame(eliminateAt(sf.getStackFrame()));
            }
        });
        return stck;
    }

    static String eliminateAt(String stackFrame) {
        StackFrame sf = new StackFrame(stackFrame);
        sf.setAt("");

        return sf.toString();

    }
}
