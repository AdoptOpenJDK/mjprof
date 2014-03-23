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

package com.performizeit.mjstack.parser;

import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.JStackMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by life on 23/2/14.
 */
public class JStackDump {
    JStackHeader header;
    ArrayList<JStackMetadataStack> stacks;
    public JStackDump(String stringRep) {
        String[] splitTraces = stringRep.split("\n\"");

        header = new JStackHeader(splitTraces[0]);
        stacks = new ArrayList<JStackMetadataStack>();
        for(int i=1;i<splitTraces.length;i++) {
            stacks.add(new JStackMetadataStack("\""+splitTraces[i]));
        }
    }
    private JStackDump() {

    }

    public ArrayList<JStackMetadataStack> getStacks() {
        return stacks;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(header+"\n\n");

        for (JStackMetadataStack stack : stacks) {
            s.append(stack.toString()+"\n");
        }

        return s.toString();
    }
    public JStackDump filterDump(JStackFilter filter) {
        JStackDump that = new JStackDump();
        that.header = header;
        that.stacks = new ArrayList<JStackMetadataStack>();
        for (JStackMetadataStack stk: stacks) {
            if (filter.filter(stk)) {
                that.stacks.add(stk);
            }
        }
        return that;
    }
    public JStackDump mapDump(JStackMapper mapper) {
        JStackDump that = new JStackDump();
        that.header = header;
        that.stacks = new ArrayList<JStackMetadataStack>();
        for (JStackMetadataStack stk: stacks) {
            that.stacks.add(mapper.map(stk));
        }
        return that;
    }
    public JStackDump sortDump(Comparator<JStackMetadataStack> comp) {
        JStackDump that = new JStackDump();
        that.header = header;
        that.stacks = new ArrayList<JStackMetadataStack>();
        for (JStackMetadataStack stk: stacks) {
            that.stacks.add(stk);
        }
        Collections.sort(that.stacks,comp);
        return that;
    }

    public JStackHeader getHeader() {
        return header;
    }
}
