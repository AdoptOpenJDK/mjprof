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

package com.performizeit.mjstack.combiners;

import com.performizeit.mjstack.api.DumpReducer;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.ThreadInfoAggregator;
import com.performizeit.mjstack.api.Param;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import java.util.Arrays;

import static com.performizeit.mjstack.parser.ThreadInfoProps.*;


@Plugin(name="merge", params ={@Param("attribute")},description="Combine all dumps to a single one merge based on thread id attribute")
public class AccumulateDumps implements DumpReducer {

    ThreadInfoAggregator tidAggr;
    int countDumps=0;

    public AccumulateDumps(String prop) {
        String[] a = {TID};
        tidAggr =new ThreadInfoAggregator(Arrays.asList(a));
    }

    public void reduce(ThreadDump td) {
        for (ThreadInfo ti :td.getStacks()) {
            tidAggr.accumulateThreadInfo(ti);
        }
        countDumps++;


    }

    @Override
    public ThreadDump getResult() {
        ThreadDump td = new ThreadDump();
        td.setHeader("Profiling session number of dumps is "+ countDumps);
        td.setStacks(tidAggr.getAggrInfos());
        return td;
    }


}
