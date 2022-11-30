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
package com.performizeit.mjprof;

import com.performizeit.mjprof.monads.MJStep;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MJProfTest {
    @Test
    public void argWhichContainsPeriod() {
        String args =  "contains/key.k,val.val/";
        ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0).getStepName(), "contains");
        assertEquals(steps.get(0).getStepArgs(), Arrays.asList("key.k", "val.val"));
    }
    @Test
    public void argWhichContainsPeriodSplit() {
        String args =  "contains/key.k,val.val/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "contains/key.k,val.val/");
        assertEquals(steps.get(1), "list");

    }
    @Test
    public void twoTrailingCommas() {
        String args =  "contains/key:k,1,1,,/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "contains/key:k,1,1,,/");
        assertEquals(steps.get(1), "list");

    }


    @Test
    public void argWhichContainsSlashSplit() {
        String args =  "path/lib/dd/a.kk/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "path/lib/dd/a.kk/");
        assertEquals(steps.get(1), "list");

    }
    @Test
    public void argWhichContainsSlashSplit2() {
        String args =  "path/lib/dd/a.txt/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "path/lib/dd/a.txt/");
        assertEquals(steps.get(1), "list");

    }
    @Test
    public void argWhichContainsPeriod2() {
        String args =  "contains/key.k,val.,,val/.contains/stack,com.performizeit/";
        ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.size(),2);
        assertEquals(steps.get(0).getStepName(), "contains");
        assertEquals(steps.get(0).getStepArgs(), Arrays.asList("key.k", "val.,val"));
        assertEquals(steps.get(1).getStepName(), "contains");
        assertEquals(steps.get(1).getStepArgs(), Arrays.asList("stack", "com.performizeit"));
    }


    @Test
    public void pathWithSlash() {
        String args =  "path//tmp/bn.txt/.contains/name,Timer/.bottom/3/";
        ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.size(),3);
        assertEquals(steps.get(0).getStepName(), "path");
        assertEquals(steps.get(0).getStepArgs(), List.of("/tmp/bn.txt"));
        assertEquals(steps.get(1).getStepName(), "contains");
        assertEquals(steps.get(1).getStepArgs(), Arrays.asList("name", "Timer"));
        assertEquals(steps.get(2).getStepName(), "bottom");
        assertEquals(steps.get(2).getStepArgs(), List.of("3"));
    }

    @Test
    public void pathWithSlashAndBackslash() {
        String args =  "path//tmp/bn.txt/.path/c:\\hello\\myfolder\\myfile.txt/";
        ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.size(),2);
        assertEquals(steps.get(0).getStepName(), "path");
        assertEquals(steps.get(0).getStepArgs(), List.of("/tmp/bn.txt"));
        assertEquals(steps.get(1).getStepName(), "path");
        assertEquals(steps.get(1).getStepArgs(), List.of("c:\\hello\\myfolder\\myfile.txt"));
    }

    @Test
    public void argListContainsSpaces() {
        String args =  "contains/key.k,val.,,val/.contains/stack,com performizeit/";
        ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.size(),2);
        assertEquals(steps.get(0).getStepName(), "contains");
        assertEquals(steps.get(0).getStepArgs(), Arrays.asList("key.k", "val.,val"));
        assertEquals(steps.get(1).getStepName(), "contains");
        assertEquals(steps.get(1).getStepArgs(), Arrays.asList("stack", "com performizeit"));
    }
    @Test
    public void spaceOutsideAnArgumentIsASeparator() {
        String args =  "contains/key.k,val.,,val/ contains/stack,com performizeit/";
        ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.size(),2);
        assertEquals(steps.get(0).getStepName(), "contains");
        assertEquals(steps.get(0).getStepArgs(), Arrays.asList("key.k", "val.,val"));
        assertEquals(steps.get(1).getStepName(), "contains");
        assertEquals(steps.get(1).getStepArgs(), Arrays.asList("stack", "com performizeit"));
    }
    
    @Test
    public void notValidStep() {
    	String args ="notValid";
    	ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
    	assertNull(steps);
    }
 

}
