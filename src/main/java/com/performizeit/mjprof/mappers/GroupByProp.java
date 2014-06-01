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

package com.performizeit.mjprof.mappers;

import com.performizeit.mjprof.api.DumpMapper;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.ThreadInfoAggregator;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;

import java.util.ArrayList;


@Plugin(name="group", params ={@Param("attr")},description="Group by an attribute")
public class GroupByProp implements DumpMapper {
    private final String prop;
    public GroupByProp(String prop) {
        this.prop = prop;
    }
    public ThreadDump map(ThreadDump jsd ) {
        ArrayList<String> a= new ArrayList<String>();
        a.add(prop);
        ThreadInfoAggregator aggr = new ThreadInfoAggregator(a);
        for (ThreadInfo mss : jsd.getStacks()  ) {
            aggr.accumulateThreadInfo(mss);
        }
       jsd.setStacks(aggr.getAggrInfos());
        return jsd;
    }



}
