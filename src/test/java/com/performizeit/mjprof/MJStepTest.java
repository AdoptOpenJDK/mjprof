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
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MJStepTest  {
    @Test
    public void twoArgs() {
        MJStep step = new MJStep("contains/key,val/");
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArgs(), Arrays.asList("key", "val"));
    }
    @Test
    public void oneArg() {
        MJStep step = new MJStep("contains/key/");
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArgs(), List.of("key"));
    }

    @Test
    public void noArgs() {
        MJStep step = new MJStep("contains//");
        assertEquals(step.getStepName(), "contains");
        assertEquals(0,step.getStepArgs().size());

    }
    @Test
    public void noArgs2() {
        MJStep step = new MJStep("contains");
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArgs().size(), 0);

    }
    @Test
    public void doubleCommas() {
        MJStep step = new MJStep("contains/key,,k,val,,val/");
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArgs(), Arrays.asList("key,k", "val,val"));
    }

    @Test
    public void argWhichContainsPeriod() {
        MJStep step = new MJStep("contains/key.k,val.val/");
        assertEquals(step.getStepName(), "contains");
        assertEquals(step.getStepArgs(), Arrays.asList("key.k", "val.val"));
    }
}
