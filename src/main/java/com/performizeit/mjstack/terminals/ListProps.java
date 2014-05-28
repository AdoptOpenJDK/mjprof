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

import com.performizeit.mjstack.api.Terminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import java.util.HashSet;


@Plugin(name="list", params ={},description="lists the possible stack trace attributes")
public class ListProps implements Terminal {
    HashSet<String> propsHash = new HashSet<String>();

    public void addStackDump(ThreadDump jsd) {
        for (ThreadInfo mss : jsd.getStacks()  ) {
            for (String prop : mss.getProps()) {
               propsHash.add(prop);
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key: propsHash) {
            sb.append(key+"//\n");

        }
        return sb.toString();
    }
}
