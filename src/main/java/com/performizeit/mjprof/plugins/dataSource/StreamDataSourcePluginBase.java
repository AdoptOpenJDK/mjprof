package com.performizeit.mjprof.plugins.dataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import com.performizeit.mjprof.parser.ThreadDumpTextualParser;
import com.performizeit.mjprof.plugin.types.DataSource;
import com.performizeit.mjprof.parser.ThreadDump;
import com.performizeit.plumbing.GeneratorHandler;


public abstract class StreamDataSourcePluginBase implements DataSource, GeneratorHandler<ThreadDump> {
  protected BufferedReader reader;

  String nextDump = null;

  boolean isDoneFlag = false;


  protected String getStackStringFromReader() {
    initReader();
    StringBuilder linesOfStack = new StringBuilder();
    String line;
    try {
      Pattern startWithADate = Pattern.compile("^(\\d\\d\\d\\d-\\d\\d-\\d\\d .*)");
      while ((line = reader.readLine()) != null) {
        //  System.out.println(line);
        if (line.length() > 0 && startWithADate.matcher(line).matches()) {   //starting a new stack dump
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

    return null;
  }

  protected void initReader() {
  }


  @Override
  public ThreadDump generate() {
    nextDump = getStackStringFromReader();
    if (nextDump == null) {
      isDoneFlag = true;
      return null;
    }
    return ThreadDumpTextualParser.parseStringRepresentation(nextDump);
  }

  @Override
  public boolean isDone() {
    return isDoneFlag;
  }

  @Override
  public void sleepBetweenIteration() {
  }
}
