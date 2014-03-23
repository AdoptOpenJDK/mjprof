package com.performizeit.mjstack.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;

import com.performizeit.mjstack.parser.JStackMetadataStack;


public class PluginUtils {

	private static final String MAPPER_INTERFACE = "com.performizeit.mjstack.mappers.JStackMapper";
	private static final String FILTER_INTERFACE = "com.performizeit.mjstack.filters.JStackFilter";

	/*
	 *  @param clazz - class to invoke
	 *  @param js - the arg to the excute method
	 *  @param conParameter - parameter to pass to the class constructor 
	 */
	public static Object runPlugin(Class<?> clazz,JStackMetadataStack js,String conParameter) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		String methodName = getMethodName(clazz);
		if (conParameter==null){
			Constructor<?> constructor = clazz.getConstructor();
			Object obj = constructor.newInstance();
			Method excuteMethod = clazz.getMethod(methodName, JStackMetadataStack.class);
			return excuteMethod.invoke(obj,js);
		}
		//	}else {
		Constructor<?> constructor = clazz.getConstructor(conParameter.getClass());
		Object obj = constructor.newInstance(conParameter);
		Method excuteMethod = clazz.getMethod(methodName, JStackMetadataStack.class);
		return excuteMethod.invoke(obj,js);

	}

	private static String getMethodName(Class<?> clazz) {
		if(isImplementsMapper(clazz)){
			 return "map";
		}else if(isImplementsFilter(clazz)){
			return "filter";
		}else {
			System.out.println(clazz.getName() + "isn't implementing Mapper or Filter interface");
		}
		return null;
	}

	//conParameter - constructor parameters
	public static HashMap<String,Class> getAllPlugins() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		HashMap<String,Class> map = new HashMap<String,Class>();
		//TODO
		Reflections reflections = new Reflections("com.performizeit");
		Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);

		for(Class cla :annotatedPlugin){
			String helpLine=invokeGetHelpLine(cla);	
			map.put(helpLine, cla);
		}
		return map;
	}

	private static String invokeGetHelpLine(Class<?> cla) throws SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		Constructor<?> con;
		Object obj;
		try {
			con = cla.getConstructor();
			obj = con.newInstance();
		} catch (NoSuchMethodException e) {
			con = cla.getConstructor(String.class);
			obj = con.newInstance(" ");
		}
		Method excuteMethod = cla.getMethod("getHelpLine");
		return (String) excuteMethod.invoke(obj);
	}

	private static boolean isImplementsPlugin(Class<?> cla,String pluginType) {
		Class[] k = cla.getInterfaces();
		for(int i=0;i<k.length;i++){
			if(k[i].getName().equals(pluginType))
				return true;
		}
		return false;
	}
	private static boolean isImplementsMapper(Class<?> cla) {
		return isImplementsPlugin(cla,MAPPER_INTERFACE);
	}
	private static boolean isImplementsFilter(Class<?> cla) {
		return isImplementsPlugin(cla,FILTER_INTERFACE);
	}

}
