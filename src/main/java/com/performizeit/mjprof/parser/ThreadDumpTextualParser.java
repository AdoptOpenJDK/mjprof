package com.performizeit.mjprof.parser;

import com.performizeit.mjprof.model.JStackHeader;

import java.util.ArrayList;

public class ThreadDumpTextualParser {
  public static String JNI_GLOBAL_REFS = "JNI global references:";

  public static ThreadDump parseStringRepresentation (String stringRep) {
    String[] splitTraces = stringRep.split("\n\"");  // Assuming that thread stack trace starts with a new line followed by "
    int JNIglobalReferences = -1;
    JStackHeader header = new JStackHeader(splitTraces[0]);
    ArrayList<ThreadInfo> threadInfos = new ArrayList<>();
    for (int i = 1; i < splitTraces.length; i++) {
      if (splitTraces[i].startsWith(JNI_GLOBAL_REFS)) {
        try {
          JNIglobalReferences = Integer.parseInt(splitTraces[i].substring(splitTraces[i].indexOf(":") + 2).trim());
        } catch (NumberFormatException e) {
          // do nothing so we missed the JNI global references I do not know what to do with it.
        }

      } else {
        threadInfos.add(new ThreadInfo("\"" + splitTraces[i]));
      }
    }
    return new ThreadDump(header,threadInfos,JNIglobalReferences);
  }
}
