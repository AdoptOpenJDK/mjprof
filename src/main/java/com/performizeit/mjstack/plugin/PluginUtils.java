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

package com.performizeit.mjstack.plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import com.performizeit.mjstack.api.*;
import org.reflections.Reflections;


public class PluginUtils {


	public static Object initObj(Class<?> clazz, Class[] paramTypes,Object[] paramArgs) {
        try {
        	Constructor<?>  constructor = clazz.getConstructor(paramTypes);
        	return constructor.newInstance(paramArgs);
        } catch (Exception e) {
       	return new RuntimeException(e.getMessage());
        }
	}




	//conParameter - constructor parameters
	public static HashMap<Class,Class> getAllPlugins() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		HashMap<Class,Class> map = new HashMap<Class,Class>();
		//TODO
		Reflections reflections = new Reflections("com.performizeit");
		Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);

		for(Class cla :annotatedPlugin){
			if(BasePlugin.class.isAssignableFrom(cla)){
				String helpLine=invokeGetHelpLine(cla);	
				map.put(cla, cla);
			}else{
				System.out.println("class " + cla.getName() + " needs to extend BasePlugin child");
			}
		}
		return map;
	}

	private static String invokeGetHelpLine(Class<?> cla)  {
	    Plugin pluginAnnotation = cla.getAnnotation(Plugin.class);
        return pluginAnnotation.description();
	}

	public static boolean isMapper(Object o ) {
		return Mapper.class.isAssignableFrom(o.getClass());
	}
    public static boolean isDumpMapper(Object o) {
        return DumpMapper.class.isAssignableFrom(o.getClass());
    }
    public static boolean isDumpReducer(Object o) {
        return DumpReducer.class.isAssignableFrom(o.getClass());
    }
	public static boolean isFilter(Object o) {
		return Filter.class.isAssignableFrom(o.getClass());
	}

	public static boolean isTerminal(Object o) {
		return Terminal.class.isAssignableFrom(o.getClass());
	}

	public static boolean isComparator(Object o) {
		return ThreadInfoComparator.class.isAssignableFrom(o.getClass());
	}
	public static boolean isDataSource(Object o) {
		return DataSource.class.isAssignableFrom(o.getClass());
	}
	
}
