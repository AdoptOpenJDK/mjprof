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
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;
import java.util.HashMap;

import static com.performizeit.mjstack.parser.ThreadInfoProps.*;


@Plugin(name="merge", paramTypes={String.class},description="combine all dumps to a single one")
public class AccumulateDumps implements DumpReducer {

    public ThreadDump reduce(ThreadDump jsd1, ThreadDump jsd2) {
        HashMap<Long,ThreadInfo> threadMap = new HashMap<Long, ThreadInfo>();
        for (ThreadInfo ti : jsd1.getStacks()  ) {
          long a = (Long)ti.getVal(TID+"Long");
          threadMap.put(a,ti);
        }
        for (ThreadInfo ti2 : jsd1.getStacks() ) {
           ThreadInfo ti1 = threadMap.get(ti2.getVal(TID+"Long")) ;
           if (ti1 != null) {
               Profile p1 = (Profile) ti1.getVal(STACK);
               Profile p2 = (Profile) ti2.getVal(STACK);
               p1.addMulti(p2);
           }  else {
               jsd1.addThreadInfo(ti2);

           }
        }

        return jsd1;
    }


}
