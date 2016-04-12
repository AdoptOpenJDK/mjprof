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

package com.performizeit.mjprof.plugins.combiners;

import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.ThreadInfoAggregator;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.plugin.types.DumpReducer;
import com.performizeit.plumbing.PipeHandler;

import java.util.ArrayList;


@Plugin(name = "merge", params = {@Param(type = String.class, value = "attr", optional = true, defaultValue = "tid")}, description = "Combine all dumps to a single one merge based on an attribute (thread id is the default attribute)")
public class AccumulateDumps implements DumpReducer, PipeHandler<ThreadDump, ThreadDump> {

  ThreadInfoAggregator tidAggr;
  int countDumps = 0;

  public AccumulateDumps(String prop) {
    ArrayList<String> a = new ArrayList<String>();
    a.add(prop);
    tidAggr = new ThreadInfoAggregator(a);
  }

  public void reduce(ThreadDump td) {
    for (ThreadInfo ti : td.getStacks()) {
      tidAggr.accumulateThreadInfo(ti);
    }
    countDumps++;


  }

  public ThreadDump getResult() {
    ThreadDump td = new ThreadDump();
    td.setHeader("Profiling session number of dumps is " + countDumps);

    td.setStacks(tidAggr.getAggrInfos());
    return td;
  }


  @Override
  public ThreadDump handleMsg(ThreadDump msg) {
    reduce(msg);
    return null;
  }

  @Override
  public ThreadDump handleDone() {
    return getResult();
  }


}
