/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.performizeit.mjprof.model;

import com.performizeit.mjprof.plugins.filters.ProfileNodeFilter;

import java.util.HashMap;


public class Profile {
    public boolean color = false;
    SFNode root =     new SFNode();

    public Profile() {
        root.setColor(color);
        root.sf = null;
    }
    
    public String test(){
    	return root.sf;
    }
    public  Profile(String parseString) {
        this();
        if (parseString.contains("]\\ ")) {
            parseMulti(parseString);
        }   else {
            parseSingle(parseString);
        }
    }  

	public  void parseSingle(String stackTrace) {
        addSingle(stackTrace);
    }


    public void addSingle(String stackTrace) {
        if (stackTrace.trim().length()==0) return;  // I am not sure that this is the correct
        //// decision but when no stack trace then the profile should be  nullified as well
        String[] sf = stackTrace.split("\n");
        HashMap<String,SFNode> c = root.children;
        root.count ++;
        for (int i=sf.length-1;i>=0;i--) {

            String sfi = sf[i].trim();
            if (sfi.isEmpty()) continue;
            SFNode node = c.get(sfi);
            if (node == null) {
                node = new SFNode();

                node.sf = sfi;
                c.put(sfi, node);
            }
            node.count++;
            c = node.children;
        }
    }
    public void addSingle(StackTraceElement[] elements) {
        if (elements.length==0) return;  // I am not sure that this is the correct
        //// decision but when no stack trace then the profile should be  nullified as well
        HashMap<String,SFNode> c = root.children;
        root.count ++;
        for (int i=elements.length-1;i>=0;i--) {
            String sfi = "at "+ elements[i].getClassName() + "." +elements[i].getMethodName() +"("+elements[i].getFileName()+":"+elements[i].getLineNumber()+")";
            SFNode node = c.get(sfi);
            if (node == null) {
                node = new SFNode();

                node.sf = sfi;
                c.put(sfi, node);
            }
            node.count++;
            c = node.children;
        }
    }

    public  void parseMulti(String treeString) {
        String[] lines = treeString.split( "\n");
        nextFrame(-1,root, lines, 0);

    }



    private int nextFrame(int parentPehIndent,SFNode parent,  String[] lines, int curLine) {
        while (curLine < lines.length) {
            String line = lines[curLine];

            ProfileEntryHelper peh = new ProfileEntryHelper(line);
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
    
    public void addMulti(Profile p) {
        root.mergeToNode(p.root);
    }
      
    @Override
    public String toString() {
        return root.toString();

    }

    public void visit(ProfileVisitor pv) {
         root.visitChildren(pv,0);
    }

    public void filter(ProfileNodeFilter pnf, Object context) {
        root.filterChildren(pnf,0,context);
    }
    public int getCount() {
        return root.getCount();
    }

	public void filterDown(ProfileNodeFilter profileNodeFilter, Object context) {
		  root.filterDownChildren(profileNodeFilter,0,context);		
	}
	
	public void filterUp(ProfileNodeFilter profileNodeFilter, Object context) {	
		  root.filterUpChildren(profileNodeFilter,0,context);		
	}

}
