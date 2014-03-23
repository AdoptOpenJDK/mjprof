package com.performizeit.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;


public class AnnotaionUtils {

	private static final String PLUGIN_INTERFACE = "com.performizeit.annotation.Plugin";

	/*
	 *  @param clazz - class to invoke
	 *  @param js - the arg to the excute method
	 *  @param conParameter - parameter to pass to the class constructor 
	 */
	public static void runPlugin(Class<?> clazz,com.performizeit.mjstack.parser.JStackMetadataStack js,String conParameter) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		//if(isImplementsPlugin(clazz)){
			Constructor<?> constructor = clazz.getConstructor(conParameter.getClass());
			Object obj = constructor.newInstance(conParameter);
			Method excuteMethod = clazz.getMethod("excute", com.performizeit.mjstack.parser.JStackMetadataStack.class);
			excuteMethod.invoke(obj,js);
	}

	//conParameter - constructor parameters
	public static HashMap<String,Class> getAllPlugins(com.performizeit.mjstack.parser.JStackMetadataStack s,String conParameter) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		HashMap<String,Class> map = new HashMap<String,Class>();
		//TODO
		Reflections reflections = new Reflections("com.performizeit");
		Set<Class<?>> annotatedMapper = reflections.getTypesAnnotatedWith(Mapper.class);
		Set<Class<?>> annotatedFilter = reflections.getTypesAnnotatedWith(Filter.class);

		for(Class cla :annotatedMapper){
			if(isImplementsPlugin(cla)){
				String helpLine=invokeGetHelpLine(cla,conParameter);	
				map.put(helpLine, cla);
			}else{
				System.out.println("Warning:" + cla.getName() + " not implement Plugin interface");
			}
		}
		return map;
	}

	private static String invokeGetHelpLine(Class<?> cla, String conParameter) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<?> con = cla.getConstructor(conParameter.getClass());
		Object obj = con.newInstance(conParameter);
		Method excuteMethod = cla.getMethod("getHelpLine");
		return (String) excuteMethod.invoke(obj);
	}
	
	private static boolean isImplementsPlugin(Class<?> cla) {
		Class[] k = cla.getInterfaces();
		for(int i=0;i<k.length;i++){
			System.out.println(k[i].getName());
			if(k[i].getName().equals(PLUGIN_INTERFACE))
				return true;
		}
		return false;
	}

}
