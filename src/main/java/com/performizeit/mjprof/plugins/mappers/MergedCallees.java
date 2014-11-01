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

package com.performizeit.mjprof.plugins.mappers;

import com.performizeit.mjprof.plugin.types.DumpReducer;
import com.performizeit.mjprof.plugins.mappers.singlethread.stackframe.FileNameEliminator;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.model.ThreadInfoAggregator;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.plumbing.PipeHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Plugin(name="group", params ={@Param(type = String.class,value = "attr",optional=true,defaultValue = "")},description="Group a single thread dump by an attribute. If not attribute is specified all dump is merged")
public class MergedCallees implements DumpReducer,PipeHandler<ThreadDump,ThreadDump>  {
	private final String methodName;
	public MergedCallees(String methodName) {
		this.methodName = methodName;
	}
	public ThreadDump map(final ThreadDump jsd ) {
		ArrayList<String> a= new ArrayList<String>();
		a.add("stack");
		Profile pro= new Profile();
		ThreadInfoAggregator aggr = new ThreadInfoAggregator(a);
		for (ThreadInfo mss : jsd.getStacks()  ) {
			//    	Profile p = (Profile)mss.getVal("stack");
			String p =  preperStackTrace(mss.getVal("stack").toString());
			if(p!=null){
				pro.addMulti(new Profile(p));
				mss.setVal("stack", new Profile(p));
				aggr.accumulateThreadInfo(mss);
			}
		}
		System.out.println(pro);
		ThreadDump jsd2 = new ThreadDump();
		jsd2.setHeader(jsd.getHeader());
		jsd2.setStacks(aggr.getAggrInfos());
		jsd2.setJNIglobalReferences(jsd.getJNIglobalReferences());
		return jsd2;
	}

	@Override public ThreadDump handleMsg(ThreadDump msg) { return map(msg);}
	@Override public ThreadDump handleDone() {return null;}

	private String preperStackTrace(String stackTrace) {
		String[] sf = stackTrace.split("\n");
		for (int i=sf.length-1;i>=0;i--) {
			String sfi = sf[i].trim();
			if (sfi.isEmpty()) continue;
			// String newSfi = FileNameEliminator.eliminatePackage(sfi);
			//todo anat
			if(sfi.contains(methodName)){
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
