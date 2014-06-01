/*
       This file is part of mjprof.

        mjprof is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjprof is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with mjprof.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjprof.model;

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