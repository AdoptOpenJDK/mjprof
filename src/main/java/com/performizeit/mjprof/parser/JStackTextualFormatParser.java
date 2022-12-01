package com.performizeit.mjprof.parser;

import com.performizeit.mjprof.model.Profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.performizeit.mjprof.parser.ThreadInfoProps.*;


public class JStackTextualFormatParser {
  public static final String LOCKED_OWNABLE_SYNCHRONIZERS="Locked ownable synchronizers";
  public static final String JAVA_LANG_THREAD_STATE="java.lang.Thread.State";

  private void parseThreadHeaderLine(HashMap<String, Object> props, String metaLine) {
    Pattern p = Pattern.compile("^\"(.*)\".*");
    Matcher m = p.matcher(metaLine);

    if (m.find()) {
      props.put(NAME, m.group(1));
    }

    extractStatus(props,metaLine);
    metadataKeyValProperties(props,metaLine);

    if (metaLine.contains("\" " + DAEMON + " ")) {
      props.put(DAEMON, true);
    }
  }
  protected void metadataKeyValProperties(HashMap<String, Object> props, String metaLine) {
    Pattern p = Pattern.compile("(\\S+)=(\\S+)");    // a non space string then = and then a non space string
    Matcher m = p.matcher(metaLine);
    while (m.find()) {
      props.put(m.group(1), m.group(2));
    }
    if (props.get(TID) != null && !props.get(TID).equals("*")) {
      props.put(TID + "Long", new HexaLong((String) props.get(TID)));
    }
    if (props.get(NID) != null && !props.get(NID).equals("*")) {
      props.put(NID + "Long", new HexaLong((String) props.get(NID)));
    }
  }


  private void parseThreadState(HashMap<String, Object> props,String threadState) {
    Pattern p = Pattern.compile("^[\\s]*" + JAVA_LANG_THREAD_STATE + ": (.*)$");
    Matcher m = p.matcher(threadState);
    if (m.find()) {
      props.put(STATE, m.group(1));
    }
  }

  public HashMap<String, Object> parseLegacyTextualFormat(String stackTrace) {
    HashMap<String, Object> props = new HashMap<>();
    BufferedReader reader = new BufferedReader(new StringReader(stackTrace));
    try {
      String metaLine = reader.readLine();
      if (metaLine != null) {
        parseThreadHeaderLine(props, metaLine);

        String threadState = reader.readLine();
        if (threadState != null) {
          parseThreadState(props, threadState);
        }
        String linesOfStack = readUntilEmptyLine(reader);
        String s;
        props.put(STACK, new Profile(linesOfStack));
        while ((s = reader.readLine()) != null) {
          if (s.contains(LOCKED_OWNABLE_SYNCHRONIZERS)) {
            String linesOfLOS = readUntilEmptyLine(reader);
            if (linesOfLOS.trim().length() > 0)
              props.put(LOS, new ThreadLockedOwnableSynchronizers(linesOfLOS));
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return props;
  }

  private String readUntilEmptyLine(BufferedReader reader) throws IOException {
    StringBuilder chunk = new StringBuilder();
    String s;
    while ((s = reader.readLine()) != null) {
      if (s.trim().length() == 0) break;
      chunk.append(s).append("\n");
    }
    return chunk.toString();
  }

  private void extractStatus(HashMap<String, Object> props, String metaLine) {
    int idx = metaLine.lastIndexOf('=');
    if (idx != -1) {
      String lastParam = metaLine.substring(idx);
      idx = lastParam.indexOf(' ');
      if (idx != -1) {

        lastParam = lastParam.substring(idx + 1);

        if (lastParam.length() > 0) {
          props.put(STATUS, lastParam.trim());
        }
      }
    }
  }


  public String toJStackFormat(HashMap<String, Object> props) {
    StringBuilder mdStr = new StringBuilder();

    if (props.get(NAME) != null) {
      mdStr.append("\"").append(props.get(NAME)).append("\"");
    }
    if (props.get(COUNT) != null) {
      mdStr.append(" " + COUNT + "=").append(props.get(COUNT));
    }
    if (props.get(DAEMON) != null) {
      mdStr.append(" " + DAEMON);
    }
    if (props.get(PRIO) != null) {
      mdStr.append(" " + PRIO + "=").append(props.get(PRIO));
    }
    if (props.get(TID) != null) {
      mdStr.append(" " + TID + "=").append(props.get(TID));
    }
    if (props.get(NID) != null) {
      mdStr.append(" " + NID + "=").append(props.get(NID));
    }
    if (props.get(STATUS) != null) {
      mdStr.append(" ").append(props.get(STATUS));
    }
    if (props.get(CPUNS) != null) {
      mdStr.append(" " + CPUNS + "=").append(props.get(CPUNS));
    }
    if (props.get(WALL) != null) {
      mdStr.append(" " + WALL + "=").append(props.get(WALL));
    }
    if (props.get(CPU_PREC) != null) {
      mdStr.append(" " + CPU_PREC + "=").append(props.get(CPU_PREC));
    }


    if (props.get(STATE) != null) {
      mdStr.append("\n   " + JAVA_LANG_THREAD_STATE + ": ").append(props.get(STATE));
    }

    if (props.get(STACK) != null) {
      mdStr.append("\n").append(props.get(STACK).toString());
    }
    mdStr.append("\n");
    if (props.get(LOS) != null) {
      mdStr.append("\n   " + LOCKED_OWNABLE_SYNCHRONIZERS + ":\n").append(
          props.get(LOS).toString());
    }

    return mdStr.toString();
  }

}
