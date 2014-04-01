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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.performizeit.mjstack.api.JStackComparator;
import com.performizeit.mjstack.api.JStackFilter;
import com.performizeit.mjstack.api.JStackMapper;
import com.performizeit.mjstack.api.JStackTerminal;
import com.performizeit.mjstack.api.Plugin;
import com.performizeit.mjstack.parser.JStackMetadataStack;


public class PluginUtils {


	public static Object initObj(Class<?> clazz, Class[] paramTypes,List<String> params) {
        Object[] paramsTrans = new Object[paramTypes.length];

        for (int i=0;i<paramTypes.length;i++) {
            if (paramTypes[i].equals(Integer.class) || paramTypes[i].equals(int.class)) {
                paramsTrans[i] = Integer.parseInt(params.get(i));
            } else
            if (paramTypes[i].equals(Long.class) || paramTypes[i].equals(long.class)) {
                paramsTrans[i] = Long.parseLong(params.get(i));
            }else {
                paramsTrans[i] =   params.get(i);
            }
        }
        try {
        	Constructor<?>  constructor = clazz.getConstructor(paramTypes);
        	return constructor.newInstance(paramsTrans);
        } catch (NoSuchMethodException e) {
        	return new RuntimeException(e.getMessage());
        } catch (SecurityException e) {
        	return new RuntimeException(e.getMessage());
        } catch (InstantiationException e) {
        	return new RuntimeException(e.getMessage());
        } catch (IllegalAccessException e) {
        	return new RuntimeException(e.getMessage());
        } catch (IllegalArgumentException e) {
        	return new RuntimeException(e.getMessage());
        } catch (InvocationTargetException e) {
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
