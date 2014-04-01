package com.performizeit.mjstack.mergers;

import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackDump;
import com.performizeit.mjstack.parser.JStackMetadataStack;
import com.performizeit.mjstack.parser.JStackProps;
import com.performizeit.mjstack.parser.JStackStack;


import java.util.HashMap;
import java.util.HashSet;

@Plugin(name="tree", paramTypes={},description="combine all stack traces ")
public class StackTree implements JStackTerminal {
    protected boolean color = false;
    class SFNode {
        int count;
        String sf;
         HashMap<String,SFNode> children=     new HashMap<String,SFNode>();

        @Override
        public String toString() {

            return toString(0,new HashSet<Integer>());
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


        public String toString(int lvl,HashSet<Integer> brkPts) {

            String a = "";
            for (int i=0;i<lvl;i++) {
                if (brkPts.contains(i))  {

                    a += GREEN()+"|"+NC();
                }            else
                a += " ";
            }
            String ch = "\\";
            if (children.size() >1) {
                ch = "X";
                brkPts.add(lvl);
            }
            if (children.size()==0) {
                ch = "V";
            }
            a = String.format( "%4d ", count)+a+GREEN()+ch+NC() +" "+ sf;
            a += "\n";
            int t = 0;
            for(SFNode n: children.values())  {
                if (t == children.size()-1) {
                    brkPts.remove(lvl);
                }
                t++;
                a  += n.toString(lvl+1,brkPts);
                if (lvl ==0) a+= "\n";
            }

            return a;
        }

    }
    HashMap<String,SFNode> roots =     new HashMap<String,SFNode>();
    void transformStack(JStackStack stackTrace) {

        String[] sf = stackTrace.getStackFrames();
        HashMap<String,SFNode> c = roots;
        for (int i=sf.length-1;i>=0;i--) {
            String sfi = sf[i].trim();
            if (sfi.isEmpty()) continue;
            SFNode node = c.get(sfi);
            if (node != null) {
                node.count++;



            }  else {
                node = new SFNode();
                node.count++;
                node.sf = sfi;
                c.put(sfi,node);
            }
            c = node.children;
           // System.out.println(sf[i]);
        }



    }

    @Override
    public String toString() {
        String a = "";
        for(SFNode n: roots.values())  {
            a  += n.toString();
        }
        return a;
    }





    public void addStackDump(JStackDump jsd) {

        for (JStackMetadataStack mss : jsd.getStacks()  ) {
            transformStack((JStackStack) mss.getVal(JStackProps.STACK));      // System.out.println();

        }
    }
}
