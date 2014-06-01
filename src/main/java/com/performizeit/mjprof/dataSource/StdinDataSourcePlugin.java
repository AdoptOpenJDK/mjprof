package com.performizeit.mjprof.dataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.parser.ThreadDump;

@Plugin(name="stdin", params ={},description = "Read thread dumps from standard input")
public class StdinDataSourcePlugin extends StreamDataSourcePluginBase {
    public StdinDataSourcePlugin() {
    }
	@Override
	public ArrayList<ThreadDump> getThreadDumps() {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> stackStrings = getStackStringsFromReader(r);
		return buildJstacks(stackStrings);
	}
}
