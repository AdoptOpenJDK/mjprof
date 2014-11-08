package com.performizeit.mjprof.monads;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;


public class Macros {
    private  Properties defaultProps = null;
    private static Macros instance = new Macros();
    private String reason = "";
    private String macroFile;
    public static Macros getInstance() {
        return instance;
    }
    private  Macros() {
        macroFile = System.getProperty("macros.configFile");
        if (macroFile == null) {
            macroFile = "macros.properties";
        }
        try {
            initProp(macroFile);
        } catch (IOException e) {
            reason = "No macros Unable to read macro file '"+macroFile+"'";

        }
    }
    public  void initProp(String propFile) throws  IOException {
        defaultProps = new Properties();
        FileInputStream in = new FileInputStream(propFile);
        defaultProps.load(in);
        in.close();
    }

    public  Properties getProps() {
        return defaultProps;
    }


    public  String getProperty(String name) {
        return (String) defaultProps.get(name);
    }

    public static void main(String[] args) {
        Macros ins = getInstance();
        for (String name : ins.getProps().keySet().toArray(new String [ins.getProps().size()])) {
            System.out.println(name +":"+ ins.getProperty(name));

        }
    }
    public  void getSynopsisContent(StringBuilder sb) {
        if (defaultProps == null ) {
            sb.append(reason+"\n");
            return;
        }
        if (defaultProps.size() == 0 ) {
            sb.append("No macros defined in file '"+macroFile+"' \n");
        }
        String[] names = getProps().keySet().toArray(new String [getProps().size()]);
        Arrays.sort(names, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.startsWith("-")) o1 = o1.substring(1) + "-";
                if (o2.startsWith("-")) o2 = o2.substring(1) + "-";
                return o1.compareTo(o2);

            }
        });
        for (String name : names) {
            String ln =  "  "+name;
            sb.append(ln);
            for (int j = 0; j < (70 - ln.length()); j++) {
                sb.append(" ");
            }
            sb.append("- ");
            sb.append(getProperty(name));
            sb.append("\n");

        }
    }

}
