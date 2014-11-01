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

package com.performizeit.mjstack.mappers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.performizeit.mjstack.api.DumpMapper;
import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.model.Profile;
import com.performizeit.mjstack.model.ProfileNodeFilter;
import com.performizeit.mjstack.model.SFNode;
import com.performizeit.mjstack.parser.ThreadDump;
import com.performizeit.mjstack.parser.ThreadInfo;

import  static com.performizeit.mjstack.parser.ThreadInfoProps.*;

@Plugin(name="stackkeep",paramTypes = {String.class},
        description = "Eliminates stack frames from all stacks which do not contain string.")
public class MergedCallees implements DumpMapper {
    protected final String methodName;
    private Profile p = null;

    public MergedCallees(String expr) {
        this.methodName = expr;
        this.p= new Profile();
    }

    public ThreadDump map(ThreadDump jsd ) {
    	for (ThreadInfo mss : jsd.getStacks()  ) {
    	p=	(Profile) mss.getVal("prio");
		System.out.println(" *** "+ p);

    	String stck = preperStackTrace(mss.getVal("prio").toString());
    		if(stck == null)
    			continue;  
    		p.addMulti(new Profile(stck));
    	}    

    	ArrayList<ThreadInfo> reducedThreadInfo = new  ArrayList<ThreadInfo>();
    	reducedThreadInfo.add(new ThreadInfo(p.toString()));
    	jsd.setStacks(reducedThreadInfo);

    	return jsd ;
    }


    
    private String preperStackTrace(String stackTrace) {
    	String[] sf = stackTrace.split("\n");
    	 for (int i=sf.length-1;i>=0;i--) {
             String sfi = sf[i].trim();
             if (sfi.isEmpty()) continue;
          String newSfi = FileNameEliminator.eliminatePackage(sfi);
         if(newSfi.equals("at " + methodName)){
           String stack =  printthatpart(sf, i);
            return stack;
            }
             }
		return null;

         }

    	
	private String printthatpart(String[] sf, int j) {
		StringBuilder sb = new StringBuilder();
		 for (int i=sf.length-1;i>=j;i--) {
			 sb.append(sf[i]);
			 sb.append("\n");
		 }
		 return sb.toString();
		
	}
	


private String printthatpartUp(String[] sf, int j) {
StringBuilder sb = new StringBuilder();
for (int i=j;i<sf.length-1;i++) {
	 sb.append(sf[i]);
	 sb.append("\n");
}
return sb.toString();

}


    public static void main(String[] args) throws IOException {
    	MergedCallees g = new MergedCallees("org.eclipse.core.internal.jobs.WorkerPool.test");
	//	ThreadDump dump = new ThreadDump(readFromFile("C:/Users/8/Desktop/mjprof/src/test/java/test1.txt"));
    	ThreadDump dump = new ThreadDump(readFromFile("C:/Program Files/Java/jdk1.6.0_45/bin/test1.txt"));

System.out.println(g.map(dump));
	}

	private static String readFromFile(String fileName) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    try {
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
        String everything = sb.toString();
        return everything; //+ "\n";
        

    } finally {
        br.close();
    }
	}


}
