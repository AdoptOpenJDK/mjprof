/*
       This file is part of mjstack.

        mjstack is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjstack is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjstack.mappers;

import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileNodeFilter;
import com.performizeit.mjstack.model.SFNode;
import com.performizeit.mjstack.parser.ThreadInfo;
import com.performizeit.mjstack.model.StackTrace;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;

@Plugin(name="trimbelow",paramTypes = {String.class},
        description = "Trim all stack frames below the first occurrence of string")
public class TrimBelow implements JStackMapper {
    protected final String expr;
    int flowLevel=Integer.MAX_VALUE;


    public TrimBelow(String expr) {
        this.expr = expr;

    }

    @Override
    public ThreadInfo map(ThreadInfo stck) {
        Profile p = (Profile)stck.getVal("stack");
        p.filter(new ProfileNodeFilter() {
            @Override
            public boolean accept(SFNode node , int level,Object context) {
                String stackFrame = node.getStackFrame();
                if (level > flowLevel) {System.out.println(stackFrame+ " "+ level +"subtree");return true;}    // we are in an interesting subtree
                if (level < flowLevel) flowLevel =  Integer.MAX_VALUE;        // this signifies exiting an interesting subtree
                if  (stackFrame.contains(expr)) {
                    flowLevel = level;
                    System.out.println(stackFrame+ " "+ level +"found");
                    return true;
                }
                System.out.println(stackFrame+ " "+ level +"out");
                return false;
            }
        },null) ;
        return stck;
    }
}
