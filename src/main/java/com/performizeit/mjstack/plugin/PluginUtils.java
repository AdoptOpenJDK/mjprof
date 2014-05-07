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
	public static HashMap<String,Class> getAllPlugins() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, ClassNotFoundException{
		HashMap<String,Class> map = new HashMap<String,Class>();
		//TODO
		Reflections reflections = new Reflections("com.performizeit");
		Set<Class<?>> annotatedPlugin = reflections.getTypesAnnotatedWith(Plugin.class);

		for(Class cla :annotatedPlugin){
			if(isImplementsMapper(cla) ||isImplementsFilter(cla) ||isImplementsTerminal(cla) || isImplementsComparators(cla) ){
				String helpLine=invokeGetHelpLine(cla);	
				map.put(helpLine, cla);
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

	public static boolean isImplementsMapper(Class<?> cla) {
		return JStackMapper.class.isAssignableFrom(cla);
	}
    public static boolean isImplementsDumpMapper(Class<?> cla) {
        return DumpMapper.class.isAssignableFrom(cla);
    }
	public static boolean isImplementsFilter(Class<?> cla) {
		return JStackFilter.class.isAssignableFrom(cla);
	}

	public static boolean isImplementsTerminal(Class<?> cla) {
		return JStackTerminal.class.isAssignableFrom(cla);
	}

	public static boolean isImplementsComparators(Class cla) {
		return JStackComparator.class.isAssignableFrom(cla);
	}
	
}
