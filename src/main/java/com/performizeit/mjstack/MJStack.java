package com.performizeit.mjstack;

import com.performizeit.mjstack.filters.JStackFilterField;
import com.performizeit.mjstack.mappers.PropEliminator;
import com.performizeit.mjstack.parser.JStackDump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MJStack {
    public static void main(String[] args) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        String line;
        ArrayList<String> stackDumps = new ArrayList<String>();
        StringBuilder linesOfStack = new StringBuilder();
        while ( (line = r.readLine()) != null) {
            if (line.length()>0 && Character.isDigit(line.charAt(0))) {   //starting a new stack dump
                 if (linesOfStack.length() >0) {
                     stackDumps.add(linesOfStack.toString());
                 }
                linesOfStack = new StringBuilder();

            }
            linesOfStack.append(line).append("\n");
       }
        if (linesOfStack.length() >0) {
            stackDumps.add(linesOfStack.toString());
        }
        ArrayList<JStackDump> jStackDumps = new ArrayList<JStackDump>(stackDumps.size());
        for (String stackDump: stackDumps )     {
            JStackDump stckDump = new JStackDump(stackDump);
            stckDump = stckDump.filterDump(new JStackFilterField(args[0],args[1])).mapDump(new PropEliminator(args[2]));
            jStackDumps.add(stckDump);

        }

        //System.out.println(stackDumps.size());
        for (int i=0;i< stackDumps.size(); i++) {
            //System.out.println(jStackDumps.get(i).toString().equals(stackDumps.get(i)));

            System.out.println(jStackDumps.get(i));
        }
    }
}
