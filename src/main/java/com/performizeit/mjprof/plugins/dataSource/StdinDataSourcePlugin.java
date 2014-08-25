package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import com.performizeit.mjprof.api.Plugin;
import com.performizeit.mjprof.parser.ThreadDump;


@SuppressWarnings("unused")
@Plugin(name="stdin", params ={},description = "Read thread dumps from standard input")
public class StdinDataSourcePlugin extends StreamDataSourcePluginBase {
    public StdinDataSourcePlugin() {
        r = new BufferedReader(new InputStreamReader(System.in));
    }

    @Deprecated
	@Override
	public ArrayList<ThreadDump> getThreadDumps() {
		ArrayList<String> stackStrings = getStackStringsFromReader();
		return buildJstacks(stackStrings);
	}


}
