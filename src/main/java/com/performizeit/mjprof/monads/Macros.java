package com.performizeit.mjprof.monads;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;


public class Macros {
  private Properties defaultProps = null;
  private static final Macros instance = new Macros();
  private String reason = "";
  private String macroFile;

  public static Macros getInstance() {
    return instance;
  }

  private Macros() {
    macroFile = System.getProperty("macros.configFile");
    if (macroFile == null) {
      macroFile = "macros.properties";
    }
    try {
      initProp(macroFile);
    } catch (IOException e) {
      reason = "No macros Unable to read macro file '" + macroFile + "'";

    }
  }

  public void initProp(String propFile) throws IOException {
    defaultProps = new Properties();
    FileInputStream in = new FileInputStream(propFile);
    defaultProps.load(in);
    in.close();
  }

  public Properties getProps() {
    return defaultProps;
  }


  public String getProperty(String name) {
    return (String) defaultProps.get(name);
  }

  public void getSynopsisContent(StringBuilder sb) {
    if (defaultProps == null) {
      sb.append(reason).append("\n");
      return;
    }
    if (defaultProps.size() == 0) {
      sb.append("No macros defined in file '").append(macroFile).append("' \n");
    }
    String[] names = getProps().keySet().toArray(new String[getProps().size()]);
    Arrays.sort(names, (o1, o2) -> {
      if (o1.startsWith("-")) o1 = o1.substring(1) + "-";
      if (o2.startsWith("-")) o2 = o2.substring(1) + "-";
      return o1.compareTo(o2);

    });
    for (String name : names) {
      String ln = "  " + name;
      sb.append(ln);
      sb.append(" ".repeat(Math.max(0, (70 - ln.length()))));
      sb.append("- ");
      sb.append(getProperty(name));
      sb.append("\n");

    }
  }

}
