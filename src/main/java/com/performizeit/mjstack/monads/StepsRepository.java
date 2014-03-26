package com.performizeit.mjstack.monads;

import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;

import com.performizeit.mjstack.api.Plugin;

public class StepsRepository {
	//instead of StepProps
	static HashMap<String, StepInfo> repo = new HashMap<String, StepInfo>();

//	public StepReposetory() {
	static {
		Reflections reflections = new Reflections("com.performizeit");
		Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);
		for(Class cla: annotatedPlugin){
			Plugin pluginAnnotation = (Plugin) cla.getAnnotation(Plugin.class);
			StepInfo stepInit = new StepInfo(cla, pluginAnnotation.paramTypes(),pluginAnnotation.description());
			repo.put(pluginAnnotation.name(), stepInit);
		}
	}
	
	   public static boolean stepValid(MJStep a) {
	        StepInfo pr = repo.get(a.getStepName());
	        return pr != null && a.stepArgs.size() == pr.getArgNum();
	    }
	   
	   public static StepInfo getStep(String stepName) {
	        return  repo.get(stepName);
	    }
	   
	   public static HashMap<String, StepInfo> getRepository(){
		   return repo;
	   }
	




}
