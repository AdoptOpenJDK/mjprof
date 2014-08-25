package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import com.performizeit.mjprof.api.DataSource;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.plumbing.GeneratorHandler;


public abstract class StreamDataSourcePluginBase implements DataSource, GeneratorHandler<ThreadDump> {
    protected  BufferedReader r;
    boolean isDone = false;

    @Deprecated
	protected ArrayList<String> getStackStringsFromReader()  {
		ArrayList<String> stackDumps = new ArrayList<String>();
			while (true) {
                String stck = getStackStringFromReader();
                if (stck == null) break;
                stackDumps.add(stck);
			}
		return stackDumps;
	}

    public void setR(BufferedReader r) {
        this.r = r;
    }

    protected String getStackStringFromReader()  {
        StringBuilder linesOfStack = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                if (line.length() > 0 && Character.isDigit(line.charAt(0))) {   //starting a new stack dump
                    if (linesOfStack.length() > 0) {
                        return linesOfStack.toString();
                    }


                }
                if (line.startsWith(ThreadDump.JNI_GLOBAL_REFS))
                    linesOfStack.append("\""); // need this hack for later parsing
                linesOfStack.append(line).append("\n");

            }
        } catch (IOException e) {
            System.err.println("Error while parsing stdin" + e);
        }
        if (linesOfStack.length() > 0) {
            return linesOfStack.toString();
        }
        isDone = true;
        return null;
    }

    @Deprecated
	protected  ArrayList<ThreadDump> buildJstacks(ArrayList<String> stackStrings) {
		ArrayList<ThreadDump> jStackDumps = new ArrayList<ThreadDump>(stackStrings.size());
		for (String stackDump : stackStrings) {
			ThreadDump stckDump = new ThreadDump(stackDump);
			jStackDumps.add(stckDump);
		}
		return jStackDumps;
	}

    @Override
    public ThreadDump generate() {
        return new ThreadDump(getStackStringFromReader());
    }

    @Override
    public boolean isDone() {
          return isDone;
    }

    @Override
    public void sleepBetweenIteration() {
    }
}
