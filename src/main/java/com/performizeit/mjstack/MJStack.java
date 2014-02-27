package com.performizeit.mjstack;

import com.performizeit.mjstack.filters.JStackFilterField;
import com.performizeit.mjstack.mappers.PropEliminator;
import com.performizeit.mjstack.parser.HexaLong;
import com.performizeit.mjstack.parser.JStackDump;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.Particle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MJStack {
    public static void main(String[] args) throws IOException {


        parseCommandLine(args);
        ArrayList<String> stackStrings = getStackStringsFromStdIn();

        ArrayList<JStackDump> jStackDumps =      buildJstacks (stackStrings);

     //   stckDump = stckDump.filterDump(new JStackFilterField(args[0], args[1])).mapDump(new PropEliminator(args[2]));

        //System.out.println(stackDumps.size());
        for (int i = 0; i < jStackDumps.size(); i++) {
            //System.out.println(jStackDumps.get(i).toString().equals(stackDumps.get(i)));

            System.out.println(jStackDumps.get(i));
        }
    }

    private static void printSynopsisAndExit() {
        System.out.println("synopsis");
        System.exit(1);
    }

    private static ArrayList<MJStep> parseCommandLine(String[] args) {
        if (args.length!=1) {
            printSynopsisAndExit();
        }
        ArrayList<MJStep> mjsteps = new ArrayList<MJStep>();
        for (String s: args[0].split("\\.") ) {
            Pattern p = Pattern.compile("(.*)\\((.*)\\)");
            Matcher m = p.matcher(s);
            if (m.find()) {
                MJStep mjStep = new MJStep(m.group(1));
                System.out.println((m.group(1)));
                for (String q: m.group(2).split(",")) {
                    System.out.println(q);
                    mjStep.addStepArg(q);
                }
            }
            System.out.println(s);

        }

        System.exit(1);
        return mjsteps;
    }

    public static   ArrayList<String> getStackStringsFromStdIn() {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
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
            linesOfStack.append(line).append("\n");
        }
        }catch(IOException e) {
            System.err.println("Error while parsing stdin"+e);
        }
        if (linesOfStack.length() > 0) {
            stackDumps.add(linesOfStack.toString());
        }
        return stackDumps;

    }

    public  static ArrayList<JStackDump> buildJstacks ( ArrayList<String> stackStrings) {
        ArrayList<JStackDump> jStackDumps = new ArrayList<JStackDump>(stackStrings.size());
        for (String stackDump : stackStrings) {
            JStackDump stckDump = new JStackDump(stackDump);
            jStackDumps.add(stckDump);
        }
        return jStackDumps;
    }
}
