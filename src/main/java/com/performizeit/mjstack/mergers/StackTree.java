package com.performizeit.mjstack.mergers;


import com.performizeit.mjstack.parser.StackTrace;


import java.util.HashMap;



public class StackTree {
    protected boolean color = false;

    public StackTree() {
        root.setColor(color);
        root.sf = null;
    }



    public void addStackTree(String treeString) {
        String[] lines = treeString.split( "\n");
        nextFrame(-1,root, lines, 0);

    }
    public int nextFrame(int parentPehIndent,SFNode parent,  String[] lines, int curLine) {
        while (curLine < lines.length) {
//            System.out.println("curLine="+curLine + lines[curLine]);
            String line = lines[curLine];

            ProfileEntryHelper peh = new ProfileEntryHelper(line);

//            System.out.println("ind="+peh.indentation);
            if (peh.indentation <= parentPehIndent) {
                return curLine;
            } else if (peh.indentation > parentPehIndent) {
                SFNode node;
                node = parent.children.get(peh.description);
                if (node == null) {
                    node = new SFNode();
                    node.sf = peh.description;
                    parent.children.put(peh.description, node);
                }
                node.count += peh.count;
                if (peh.indentation ==0) parent.count += peh.count;
                curLine = nextFrame(peh.indentation, node, lines, curLine + 1);
            }
        }
        return curLine;

    }


    SFNode root =     new SFNode();
    public void addStacktrace(StackTrace stackTrace) {

        String[] sf = stackTrace.getStackFrames();
        HashMap<String,SFNode> c = root.children;
        root.count ++;
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
        return root.toString();

    }





}
