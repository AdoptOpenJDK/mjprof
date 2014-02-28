package com.performizeit.mjstack.terminals;

import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.JStackHeader;
import com.performizeit.mjstack.parser.JStackMetadataStack;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by life on 28/2/14.
 */
public class GroupByProp implements TerminalStep{
    private final String prop;
    HashMap<String,Integer> propsHash = new HashMap<String, Integer>();
    ArrayList<JStackHeader> stackDumpsHeaders = new ArrayList<JStackHeader>();

    public GroupByProp(String prop) {
        this.prop = prop;
    }
    public void addStackDump(JStackDump jsd) {
        stackDumpsHeaders.add(jsd.getHeader());
        for (JStackMetadataStack mss : jsd.getStacks()  ) {
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
        for (JStackHeader h : stackDumpsHeaders) {
             sb.append(h.toString()).append("\n");
        }
        for (String key: propsHash.keySet()) {
            sb.append(propsHash.get(key)+ " [" + key+"]\n");

        }
        return sb.toString();
    }
}
