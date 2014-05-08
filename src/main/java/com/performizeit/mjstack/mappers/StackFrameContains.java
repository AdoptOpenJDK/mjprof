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

package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileNodeFilter;
import com.performizeit.mjstack.model.SFNode;
import com.performizeit.mjstack.parser.ThreadInfo;
import  static com.performizeit.mjstack.parser.ThreadInfoProps.*;

@Plugin(name="stackkeep",paramTypes = {String.class},
        description = "Eliminates stack frames from all stacks which do not contain string.")
public class StackFrameContains implements JStackMapper {
    protected final String expr;

    public StackFrameContains(String expr) {
        this.expr = expr;
    }

    @Override
    public ThreadInfo map(ThreadInfo stck) {
        Profile p = (Profile)stck.getVal(STACK);
        p.filter(new ProfileNodeFilter() {
            @Override
            public boolean accept(SFNode node, int level,Object context) {
                return node.getStackFrame().contains(expr);
            }
        },null) ;
        return stck;
    }
}
