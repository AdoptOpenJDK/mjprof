package com.performizeit.mjstack.model;

import java.util.ArrayList;
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
        if (count ==0) return "";
        if (count == 1) return toStringSingle();
        return toStringMulti(-1, new HashSet<Integer>(), count);
    }

    private String toStringSingle() {
        String indent = "       ";
        if (sf == null && children.size() >0  ) return   children.values().iterator().next().toStringSingle();
        if (children.size() ==0) return indent+sf;

        return children.values().iterator().next().toStringSingle() + "\n"+indent +sf;
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

    public String toStringMulti(int lvl,HashSet<Integer> brkPts,int countall) {

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
            a  += n.toStringMulti(lvl + 1, brkPts, countall);
        }

        return a;
    }
    // Visit all chlidren of a node recursively with a ProfileVisitor
    public void visitChildren(ProfileVisitor pv, int level) {
        sf = pv.visit(sf,level);
        for (SFNode child : children.values()) {  // visit all childred
            child.visitChildren(pv, level + 1);

        }
    }
    // filter out children which do not match filter
    public void filterChildren(ProfileNodeFilter pnf,int level, Object context) {
        ArrayList<String> keysToRemove = new ArrayList<String>();
        for (String childKey : children.keySet()) {
            SFNode child =    children.get(childKey);
            child.filterChildren(pnf, level + 1,context);    // first filter out the children
            if (pnf.accept(child.sf, level,this) && child.children.size()==0) {
               keysToRemove.add(childKey);
            }
        }
        for (String key: keysToRemove) {
            SFNode child = children.get(key);
            children.remove(key);
            for (String grandchildKey :child.children.keySet())  {
                mergeToNode(child.children.get(key));

            }


        }
    }

    public void mergeToNode(SFNode that) {
        this.count+= that.count;
        //merge children
        for (String key : that.children.keySet()) {
            SFNode thisChild = this.children.get(key);
            SFNode thatChild = that.children.get(key);
            if (thisChild != null) {
                thisChild.mergeToNode(thatChild);

            } else {
                children.put(key,thatChild.deepClone()) ;
            }
        }
    }

    public SFNode deepClone() {
        SFNode clone = new SFNode();
        clone.count = this.count;
        clone.sf =this.sf;
        for (String key : children.keySet()) {
            clone.children.put(key, children.get(key).deepClone());
        }
        return clone;
    }
}