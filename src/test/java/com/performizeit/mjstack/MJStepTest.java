package com.performizeit.mjstack;


import static org.junit.Assert.*;

import com.performizeit.mjstack.monads.MJStep;
import org.junit.Test;



public class MJStepTest  {
    @Test
    public void twoArgs() throws Exception {
        MJStep step = new MJStep("contains/key,val/");
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArg(0), "key");
        assertEquals(step.getStepArg(1), "val");
        assertEquals(step.getStepArgs().size(), 2);

    }
    @Test
    public void oneArg() throws Exception {
        MJStep step = new MJStep("contains/key/");
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArg(0), "key");

        assertEquals(step.getStepArgs().size(), 1);

    }

    @Test
    public void noArgs() throws Exception {
        MJStep step = new MJStep("contains//");
;
        assertEquals(step.getStepName(), "contains");
        assertEquals(0,step.getStepArgs().size());

    }
    @Test
    public void noArgs2() throws Exception {
        MJStep step = new MJStep("contains");
        ;
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArgs().size(), 0);

    }
    @Test
    public void doubleCommas() throws Exception {
        MJStep step = new MJStep("contains/key,,k,val,,val/");
        ;
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArg(0), "key,k");
        assertEquals(step.getStepArg(1), "val,val");
        assertEquals(step.getStepArgs().size(), 2);

    }

}
