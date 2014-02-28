package com.performizeit.mjstack.monads;

import java.util.HashMap;

/**
 * Created by life on 28/2/14.
 */
public class StepProps {
    public static String CONTAINS = "contains";
    public static String NOT_CONTAINS = "ncontains";
    public static String ELIMINATE = "eliminate";
    public static String SORT = "sort";
    public static String SORT_DESC = "sortd";
    public static String GROUP = "group";
    public static String COUNT = "count";
    public static String LIST = "list";
    static enum StepType {
        TERMINAL ,
        FILTER,
        MAPPER
    };
        String name;
        StepType stepType;
        int argNum;

        private StepProps(String name, StepType stepType, int argNum) {
            this.name = name;
            this.stepType = stepType;
            this.argNum = argNum;
        }

    private static HashMap<String,StepProps> stepRepo = new HashMap<String, StepProps>();
    static {
        stepRepo.put("contains",new StepProps("contains",StepType.FILTER,2));
    }
}
