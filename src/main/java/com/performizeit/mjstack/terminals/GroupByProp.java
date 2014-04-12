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

package com.performizeit.mjstack.terminals;

import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import java.util.HashMap;

/**
 * Created by life on 28/2/14.
 */
@Plugin(name="group", paramTypes={String.class},description="group by an attribute")
public class GroupByProp implements JStackTerminal {
    private final String prop;
    HashMap<String,Integer> propsHash = new HashMap<String, Integer>();


    public GroupByProp(String prop) {
        this.prop = prop;
    }
    public void addStackDump(JStackDump jsd) {

        for (ThreadInfo mss : jsd.getStacks()  ) {
           // System.out.println();
            Object o = mss.getVal(prop);
            if (o != null && o.toString().trim().length()>0) {
                String key = o.toString().trim();
                if (propsHash.get(key) != null) {
                    propsHash.put(key,propsHash.get(key)+1);
                }   else {
                    propsHash.put(key,1);
                }
            }
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (String key: propsHash.keySet()) {
            sb.append(propsHash.get(key)+ " [" + key+"]\n");

        }
        return sb.toString();
    }
}
