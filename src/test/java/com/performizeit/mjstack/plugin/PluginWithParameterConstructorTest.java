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
package com.performizeit.mjstack.plugin;

import com.performizeit.mjstack.api.Mapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileNodeFilter;
import com.performizeit.mjstack.model.SFNode;
import com.performizeit.mjstack.api.Param;
import com.performizeit.mjstack.parser.ThreadInfo;
import java.util.HashMap;

@Plugin(name="test2", params = {@Param()})
public class PluginWithParameterConstructorTest implements Mapper {
    private final String expr;

    public PluginWithParameterConstructorTest(String expr) {
        this.expr = expr;
    }

    public ThreadInfo map(ThreadInfo stck) {
        HashMap<String,Object> mtd = stck.cloneMetaData();
        Profile jss = (Profile) mtd.get("stack");

        jss.filter(new ProfileNodeFilter() {

            @Override
            public boolean accept(SFNode node, int level, Object context) {
                if (node.getStackFrame() == null) return true;
                return node.getStackFrame().contains("lock");
            }
        },null);
        return stck;
    }

	public ThreadInfo execute(ThreadInfo stck) {
		return map(stck);
	}

	public String getHelpLine() {
		return "TrimBelowhelp";
	}
}
