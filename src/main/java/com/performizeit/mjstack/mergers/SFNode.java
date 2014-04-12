package com.performizeit.mjstack.mergers;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by life on 11/4/14.
 */
public class SFNode {
    boolean  color = false;
    int count;
    String sf;
    HashMap<String,SFNode> children=     new HashMap<String,SFNode>();

    @Override
    public String toString() {

        return toString(-1,new HashSet<Integer>(),count);
    }
    private String CSI()
    {
        return (char)0x1b +"[";
    }
    private String GREEN() {
        if (!color) return "";
        return  CSI()+ "1;32m";
    }
    private String NC() {
        if (!color) return "";
        return CSI() + "0m";
    }

    public boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    public String toString(int lvl,HashSet<Integer> brkPts,int countall) {

        String a = "";
        if (sf != null) {     // we do not want to print the root node
            for (int i = 0; i < lvl; i++) {
                if (brkPts.contains(i)) {

                    a += GREEN() + "|" + NC();
                } else
                    a += " ";
            }
            String ch = "\\";
            if (children.size() > 1) {
                ch = "X";
                brkPts.add(lvl);
            }
            if (children.size() == 0) {
                ch = "V";
            }
            String bb = String.format("[%d/%d]", count, countall);
            a = String.format("%6.2f%%%10s", 100f * (float) count / countall, bb) + a + GREEN() + ch + NC() + " " + sf;
            a += "\n";

        }
        int t = 0;
        for(SFNode n: children.values())  {
            if (t == children.size()-1) {
                brkPts.remove(lvl);
            }
            t++;
            a  += n.toString(lvl+1,brkPts,countall);
        }

        return a;
    }

}