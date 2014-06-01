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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class MJProfTest {
    @Test
    public void argWhichContainsPeriod() throws Exception {
        String args =  "contains/key.k,val.val/";
        ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0).getStepName(), "contains");
        assertEquals(steps.get(0).getStepArgs(), Arrays.asList("key.k", "val.val"));
    }
    @Test
    public void argWhichContainsPeriodSplit() throws Exception {
        String args =  "contains/key.k,val.val/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "contains/key.k,val.val/");
        assertEquals(steps.get(1), "list");

    }
    @Test
    public void twoTrailingCOmmas() throws Exception {
        String args =  "contains/key:k,1,1,,/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "contains/key:k,1,1,,/");
        assertEquals(steps.get(1), "list");

    }


    @Test
    public void argWhichContainsSlashSplit() throws Exception {
        String args =  "path/lib/dd/a.kk/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "path/lib/dd/a.kk/");
        assertEquals(steps.get(1), "list");

    }
    @Test
    public void argWhichContainsSlashSplit2() throws Exception {
        String args =  "path/lib/dd//.kk/.list";
        ArrayList<String> steps = MJProf.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "path/lib/dd//.kk/");
        assertEquals(steps.get(1), "list");

    }
    @Test
    public void argWhichContainsPeriod2() throws Exception {
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
    public void argListContainsSpaces() throws Exception {
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
    public void spaceOutsideAnArgumentIsASeparator() throws Exception {
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
    public void notValidStep() throws Exception {
    	String args ="notValid";
    	ArrayList<MJStep> steps = MJProf.parseCommandLine(args);
    	assertNull(steps);
    }
 

}
