package com.performizeit.mjstack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by life on 27/2/14.
 */
public class MJStep {
    String stepName;
    List<String> stepArgs;
    String stepVal;

    public MJStep(String stepName) {
        this.stepName = stepName;
        stepArgs = new ArrayList<String>();
    }

    public String getStepName() {
        return stepName;
    }

    public List<String> getStepArgs() {
        return stepArgs;
    }
    public String getStepArg(int i) {
        return stepArgs.get(i);
    }

    public void addStepArg(String arg) {
        stepArgs.add(arg);
    }
}
