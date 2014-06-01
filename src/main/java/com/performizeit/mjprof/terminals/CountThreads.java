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

package com.performizeit.mjprof.terminals;

import com.performizeit.mjprof.api.Terminal;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.model.JStackHeader;
import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.ArrayList;

@Plugin(name="count", params ={}, description="counts number of threads")
public class CountThreads implements Terminal {
    int count =0;
    ArrayList<JStackHeader> stackDumpsHeaders = new ArrayList<JStackHeader>();


    public void addStackDump(ThreadDump jsd) {
        count = 0;
        for (ThreadInfo mss : jsd.getStacks()  ) {
                count++;
        }
    }

    @Override
    public String toString() {
        return "Total number of threads is "+ count + "\n";
    }
}
