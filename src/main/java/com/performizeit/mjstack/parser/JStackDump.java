package com.performizeit.mjstack.parser;

import com.performizeit.mjstack.filters.JStackFilter;
import com.performizeit.mjstack.mappers.JStackMapper;

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
}
