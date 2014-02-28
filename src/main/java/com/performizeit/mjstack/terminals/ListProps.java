package com.performizeit.mjstack.terminals;

import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by life on 28/2/14.
 */
public class ListProps implements TerminalStep {
    HashSet<String> propsHash = new HashSet<String>();

    public void addStackDump(JStackDump jsd) {
        for (JStackMetadataStack mss : jsd.getStacks()  ) {
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
