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

import java.util.ArrayList;
import com.performizeit.mjprof.api.Param;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.model.Profile;
import com.performizeit.mjprof.model.ProfileNodeFilter;
import com.performizeit.mjprof.model.SFNode;
import com.performizeit.mjprof.model.ThreadInfoAggregator;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.mjprof.parser.ThreadInfo;
import com.performizeit.mjprof.plugin.types.DumpReducer;
import com.performizeit.plumbing.PipeHandler;



@Plugin(name="mergedCallees", params ={@Param(type = String.class,value = "methodName",optional=true,defaultValue = "")},description="merged callees for a particular method, i.e. all call traces started from this method.")
public class MergedCallees implements DumpReducer,PipeHandler<ThreadDump,ThreadDump>  {
	private static final String STACK = "stack";
	private final String methodName;
	public MergedCallees(String methodName) {
		this.methodName = methodName;
	}
	public ThreadDump map(final ThreadDump jsd ) {
		ArrayList<String> a= new ArrayList<String>();
		a.add(methodName);
		ThreadInfoAggregator aggr = new ThreadInfoAggregator(a);
		for (ThreadInfo mss : jsd.getStacks()  ) {
			Profile p = (Profile)mss.getVal(STACK);

//			p.filterUp(new ProfileNodeFilter() {
//				@Override
//				public boolean accept(SFNode node, int level,Object context) {
//					return node.contains(methodName);
//				}
//			},null) ;
//			

			p.filterDown(new ProfileNodeFilter() {
				@Override
				public boolean accept(SFNode node, int level,Object context) {
					return node.getStackFrame().contains(methodName);
				}
			},null) ;

			mss.setVal(STACK, p); 		
			aggr.accumulateThreadInfo(mss);

		}
		ThreadDump jsd2 = new ThreadDump();
		jsd2.setHeader(jsd.getHeader());
		jsd2.setStacks(aggr.getAggrInfos());
		jsd2.setJNIglobalReferences(jsd.getJNIglobalReferences());
		return jsd2;
	}

	@Override public ThreadDump handleMsg(ThreadDump msg) { return map(msg);}
	@Override public ThreadDump handleDone() {return null;}


}
