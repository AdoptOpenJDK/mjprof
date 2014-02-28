package com.performizeit.mjstack.terminals;

import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.HashMap;

/**
 * Created by life on 28/2/14.
 */
public class CountThreads implements TerminalStep{
    int count =0;


    public void addStackDump(JStackDump jsd) {
        for (JStackMetadataStack mss : jsd.getStacks()  ) {
                count++;
        }
    }

    @Override
    public String toString() {
        return "Total number of threads is "+ count + "\n";
    }
}
