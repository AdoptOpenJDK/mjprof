package com.performizeit.mjstack.dataSource;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.openmbean.CompositeData;

import com.performizeit.jmxsupport.JMXConnection;
import com.performizeit.mjstack.api.DataSourcePlugin;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.ThreadDump;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;

@Plugin(name="JMX", paramTypes={},description = "jmx")
public class JmxDataSourcePlugin implements DataSourcePlugin{

	@Override
	public ArrayList<ThreadDump> getThreadDumps() {
		return null;

	}
	
	private CompositeData[] getData(int pid) throws Exception{
		JMXConnection c = new JMXConnection(new Integer(pid).toString());
		long[] threadsIds = c.getThreadIds();
		CompositeData[] d = c.getThreads(threadsIds,10000);
		CompositeData thread = d[0] ;
		String name = (String) thread.get("threadName");
		CompositeData[] stackTraceElems = (CompositeData []) thread.get("stackTrace");
		return stackTraceElems;

	}
	
	private ArrayList<ThreadDump> buildJstacks(CompositeData[] data){
		return null;
		
	}


}
