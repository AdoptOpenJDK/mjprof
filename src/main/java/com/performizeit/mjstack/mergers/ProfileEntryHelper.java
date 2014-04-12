package com.performizeit.mjstack.mergers;

/**
 * Created by life on 10/4/14.
 */
public class ProfileEntryHelper {
    public static final char SPLIT = 'X';
    public static final char INDENT = '\\';
    public static final char LEAF = 'V';
    int count=-1;
    int countAll=-1;
    int indentation=-1;
    char charType='~';
    String description="";
    ProfileEntryHelper(String line) {
        parseLine(line);
    }
    void parseLine(String line) {
        int leftBracketIdx = line.indexOf("[");
        int rightBracketIdx = line.indexOf("]");
        int slashIdx = line.indexOf("/");

        if (leftBracketIdx == -1 || rightBracketIdx==-1 || slashIdx == -1) return ;

        count = Integer.parseInt(line.substring(leftBracketIdx+1,slashIdx));
        countAll = Integer.parseInt(line.substring(slashIdx+1,rightBracketIdx));
        for (  indentation = 0;line.charAt(indentation+rightBracketIdx+1) == ' ' || line.charAt(indentation+rightBracketIdx+1) == '|';  indentation ++);
        charType = line.charAt(indentation+rightBracketIdx+1);
        description = line.substring(indentation+rightBracketIdx+3);
    }

};