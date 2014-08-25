package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.IOException;

import com.performizeit.mjprof.plugin.types.DataSource;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.plumbing.GeneratorHandler;


public abstract class StreamDataSourcePluginBase implements DataSource, GeneratorHandler<ThreadDump> {
    protected  BufferedReader reader;
    boolean isDone = false;



    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    protected String getStackStringFromReader()  {
        StringBuilder linesOfStack = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
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
