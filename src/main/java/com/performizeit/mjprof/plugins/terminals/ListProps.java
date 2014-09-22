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

import com.performizeit.mjprof.plugin.types.Terminal;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.util.HashSet;



@Plugin(name="list", params ={},description="lists the possible stack trace attributes")
public class ListProps implements Terminal,PipeHandler<ThreadDump,String> {
    HashSet<String> propsHash = new HashSet<String>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key: propsHash) {
            sb.append(key).append("\n");

        }
        return sb.toString();
    }

    @Override public String handleMsg(ThreadDump msg) {
        for (ThreadInfo mss : msg.getStacks()  ) {
            for (String prop : mss.getProps()) {
                propsHash.add(prop);
            }
        }
        return null;
    }
    @Override public String handleDone() {
        return toString();
    }
}
