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
    public static String KEEP_TOP = "keeptop";
    public static String KEEP_BOT = "keepbot";
    public static String STACK_ELIM = "stackelim";
    public static String STACK_KEEP = "stackkeep";
    public static String HELP = "help";
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
        stepRepo.put(CONTAINS,new StepProps(CONTAINS,StepType.FILTER,2));
        stepRepo.put(NOT_CONTAINS,new StepProps(NOT_CONTAINS,StepType.FILTER,2));
        stepRepo.put(ELIMINATE,new StepProps(ELIMINATE,StepType.MAPPER,1));
        stepRepo.put(SORT,new StepProps(SORT,StepType.MAPPER,1));
        stepRepo.put(SORT_DESC,new StepProps(SORT_DESC,StepType.MAPPER,1));
        stepRepo.put(KEEP_BOT,new StepProps(KEEP_BOT,StepType.MAPPER,1));
        stepRepo.put(KEEP_TOP,new StepProps(KEEP_TOP,StepType.MAPPER,1));
        stepRepo.put(STACK_ELIM,new StepProps(STACK_ELIM,StepType.MAPPER,1));
        stepRepo.put(STACK_KEEP,new StepProps(STACK_KEEP,StepType.MAPPER,1));
        stepRepo.put(GROUP,new StepProps(GROUP,StepType.MAPPER,1));
        stepRepo.put(COUNT,new StepProps(COUNT,StepType.TERMINAL,0));
        stepRepo.put(LIST,new StepProps(LIST,StepType.TERMINAL,0));
        stepRepo.put(HELP,new StepProps(HELP,StepType.TERMINAL,0));
    }

    public static boolean stepValid(MJStep a) {
        StepProps pr = stepRepo.get(a.getStepName());
        if (pr==null) return false;
        return true;

    }
}
