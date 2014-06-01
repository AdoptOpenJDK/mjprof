package com.performizeit.mjprof.dataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.performizeit.mjprof.api.DataSource;
import com.performizeit.mjprof.parser.ThreadDump;


public abstract class StreamDataSourcePluginBase implements DataSource {

	protected ArrayList<String> getStackStringsFromReader(BufferedReader r)  {

		ArrayList<String> stackDumps = new ArrayList<String>();
		StringBuilder linesOfStack = new StringBuilder();
		String line;
		try {
			while ((line = r.readLine()) != null) {
				if (line.length() > 0 && Character.isDigit(line.charAt(0))) {   //starting a new stack dump
					if (linesOfStack.length() > 0) {
						stackDumps.add(linesOfStack.toString());
					}
					linesOfStack = new StringBuilder();

				}
				if (line.startsWith(ThreadDump.JNI_GLOBAL_REFS))
					linesOfStack.append("\""); // need this hack for later parsing
				linesOfStack.append(line).append("\n");

			}
		} catch (IOException e) {
			System.err.println("Error while parsing stdin" + e);
		}
		if (linesOfStack.length() > 0) {
			stackDumps.add(linesOfStack.toString());
		}
		return stackDumps;
	}

	protected  ArrayList<ThreadDump> buildJstacks(ArrayList<String> stackStrings) {
		ArrayList<ThreadDump> jStackDumps = new ArrayList<ThreadDump>(stackStrings.size());
		for (String stackDump : stackStrings) {
			ThreadDump stckDump = new ThreadDump(stackDump);
			jStackDumps.add(stckDump);
		}
		return jStackDumps;
	}


}
