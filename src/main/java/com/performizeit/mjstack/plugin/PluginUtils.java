/*
       This file is part of mjstack.

        mjstack is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        mjstack is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.performizeit.mjstack.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackMetadataStack;


public class PluginUtils {

	private static final String MAPPER_INTERFACE = "com.performizeit.mjstack.api.JStackMapper";
	private static final String FILTER_INTERFACE = "com.performizeit.mjstack.api.JStackFilter";
	private static final String BASE_PLUGIN_INTERFACE = "com.performizeit.mjstack.api.BasePlugin";
	private static final String TERMINAL_INTERFACE = "com.performizeit.mjstack.api.JStackTerminal";

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
		System.out.println(clazz.getName() + " isn't implementing Mapper or Filter interface");
		return null;
	}

	//TODO - FIX
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
	//TODO: try to convet each param 
	public static Object initObj(Class<?> clazz, Class[] paramTypes,List<String> params) throws NoSuchMethodException, InstantiationException,	IllegalAccessException, InvocationTargetException {

		Constructor<?> constructor = clazz.getConstructor(paramTypes);
		return constructor.newInstance(params);
	}


	//conParameter - constructor parameters
	public static HashMap<String,Class> getAllPlugins() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		HashMap<String,Class> map = new HashMap<String,Class>();
		//TODO
		Reflections reflections = new Reflections("com.performizeit");
		Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);

		for(Class cla :annotatedPlugin){
			if(isImplementsMapper(cla) ||isImplementsFilter(cla) ){
				String helpLine=invokeGetHelpLine(cla);	
				map.put(helpLine, cla);
			}else{
				System.out.println("class " + cla.getName() + " needs to implements BasePlugin");
			}
		}
		return map;
	}

	private static String invokeGetHelpLine(Class<?> cla)  {
	    Plugin pluginAnnotation = cla.getAnnotation(Plugin.class);
        return pluginAnnotation.description();
	}

	private static boolean isImplementsPlugin(Class<?> cla,String pluginType) {
		Class[] k = cla.getInterfaces();
		for(int i=0;i<k.length;i++){
			if(k[i].getName().equals(pluginType))
				return true;
		}
		return false;
	}
	public static boolean isImplementsMapper(Class<?> cla) {
		return isImplementsPlugin(cla,MAPPER_INTERFACE);
	}
	public static boolean isImplementsFilter(Class<?> cla) {
		return isImplementsPlugin(cla,FILTER_INTERFACE);
	}

	private static boolean isImplementsBasePlugin(Class cla) {
		return isImplementsPlugin(cla,BASE_PLUGIN_INTERFACE);
	}

	public static boolean isImplementsTerminal(Class<?> cla) {
		return isImplementsPlugin(cla,TERMINAL_INTERFACE);
	}
	
	
	
}
