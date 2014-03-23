package com.performizeit.mjstack.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

import org.reflections.Reflections;

import com.performizeit.mjstack.mappers.JStackMapper;
import com.performizeit.mjstack.filters.JStackFilter;
import com.performizeit.mjstack.parser.JStackMetadataStack;


public class PluginUtils {

	private static final String MAPPER_INTERFACE = "com.performizeit.mjstack.mappers.JStackMapper";
	private static final String FILTER_INTERFACE = "com.performizeit.mjstack.filters.JStackFilter";

	/*
	 *  @param clazz - class to invoke
	 *  @param js - the arg to the execute method
	 *  @param conParameter - parameter to pass to the class constructor 
	 */
	public static Object runPlugin(Class<?> clazz,JStackMetadataStack js,String conParameter) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		Object obj = initObj(clazz, conParameter);

		if(isImplementsMapper(clazz)){
			return ((JStackMapper) obj).map(js);
		} else  if(isImplementsFilter(clazz)){
			return ((JStackFilter) obj).filter(js);
		}
		System.out.println(clazz.getName() + "isn't implementing Mapper or Filter interface");
		return null;
	}

	private static Object initObj(Class<?> clazz, String conParameter)
			throws NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Object obj;
		if (conParameter==null){
			Constructor<?> constructor = clazz.getConstructor();
			obj = constructor.newInstance();
		}else {
			Constructor<?> constructor = clazz.getConstructor(conParameter.getClass());
			obj = constructor.newInstance(conParameter);
		}
		return obj;
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
		Method executeMethod = cla.getMethod("getHelpLine");
		return (String) executeMethod.invoke(obj);
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
