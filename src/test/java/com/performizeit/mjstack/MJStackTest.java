package com.performizeit.mjstack;

import com.performizeit.mjstack.monads.MJStep;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class MJStackTest {
    @Test
    public void argWhichContainsPeriod() throws Exception {
        String args =  "contains/key.k,val.val/";
        ArrayList<MJStep> steps = MJStack.parseCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0).getStepName(), "contains");
        assertEquals(steps.get(0).getStepArgs(), Arrays.asList("key.k", "val.val"));
    }
    @Test
    public void argWhichContainsPeriodSplit() throws Exception {
        String args =  "contains/key.k,val.val/.list";
        ArrayList<String> steps = MJStack.splitCommandLine(args);
        assertNotNull(steps);
        assertEquals(steps.get(0), "contains/key.k,val.val/");
        assertEquals(steps.get(1), "list");

    }
    @Test
    public void argWhichContainsPeriod2() throws Exception {
        String args =  "contains/key.k,val.,,val/.contains/stack,com.performizeit/";
        ArrayList<MJStep> steps = MJStack.parseCommandLine(args);
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
        ArrayList<MJStep> steps = MJStack.parseCommandLine(args);
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
        ArrayList<MJStep> steps = MJStack.parseCommandLine(args);
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
    	ArrayList<MJStep> steps = MJStack.parseCommandLine(args);
    	assertNull(steps);
    }
 

}
