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

import com.performizeit.mjstack.api.DumpMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;
import  static com.performizeit.mjstack.parser.ThreadInfoProps.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by life on 28/2/14.
 */
@Plugin(name="group", paramTypes={String.class},description="group by an attribute")
public class GroupByProp implements DumpMapper {
    private final String prop;
    public GroupByProp(String prop) {
        this.prop = prop;
    }
    public ThreadDump map(ThreadDump jsd ) {
        HashMap<String,ThreadInfo> threadMap = new HashMap<String, ThreadInfo>();
        for (ThreadInfo mss : jsd.getStacks()  ) {
            // System.out.println();
            Object o = mss.getVal(prop);
            if (o != null && o.toString().trim().length()>0) {
                String key = o.toString().trim();
                if (threadMap.get(key) == null) {
                    threadMap.put(key,overlapAllPropsNotInGroupBy(mss));
                    mss.setVal("count",1);
                }   else {
                    ThreadInfo x = threadMap.get(key);
                    Profile p = (Profile)x.getVal("stack");
                    x.setVal("count",((Integer)x.getVal("count"))+1);
                    p.addMulti((Profile)mss.getVal("stack"));
                }
            }
        }
        ArrayList<ThreadInfo> reducedThreadInfo = new  ArrayList<ThreadInfo>();
        reducedThreadInfo.addAll(threadMap.values());

        jsd.setStacks(reducedThreadInfo);
        return jsd;
    }
    ThreadInfo overlapAllPropsNotInGroupBy(ThreadInfo mss) {
        ArrayList<String> keys = new ArrayList<String>(mss.getProps());
        for (String prop1: keys) {
            if (!prop1.equals(prop) && !prop1.equals(STACK) && !prop1.equals(COUNT)) {
               mss.setVal(prop1,"*");
            }
        }
        mss.remove(DAEMON);
        return mss;
    }


}
