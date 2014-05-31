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

package com.performizeit.mjstack.monads;

import com.performizeit.mjstack.api.Param;

public class StepInfo {
    private final Class pluginType;
    private Class clazz;
	private Param[] params;
	private String description;
	
	public StepInfo(Class clazz, Param[] params,String description) {
		this.clazz=clazz;
		this.params=params;
		this.description=description;
        this.pluginType =   getPluginType(clazz);
      //  System.out.println(clazz.getSimpleName() +" "+ (pluginType != null ?pluginType.getSimpleName():"null"));
	}
    Class getPluginType(Class clazz) {
        Class interfaces[] = clazz.getInterfaces();
        if (interfaces.length == 0 ) {
            Class superc = clazz.getSuperclass();
            if (superc == null)  return null;
            return getPluginType(superc);
        }
        for (Class i : interfaces) {
         // todo    if (i.isAssignableFrom(BasePlugin.class)) return i;
            return i;
        }
        Class superc = clazz.getSuperclass();
        if (superc == null)  return null;
        return getPluginType(superc);
    }

	public Class getClazz() {
		return clazz;
	}

	public Class[] getParamTypes() {
		Class[] paramTypes = new Class[params.length];

        for (int i=0;i<params.length;i++)    {
            paramTypes[i]  = params[i].type();
        }
        return paramTypes;
	}

	public String getDescription() {
		return description;
	}
	
	public int getArgNum(){
		return params.length;
	}
	
	@Override
	public String toString() {
		return "name "+ clazz + " description " + description + " param " + params;
	}

    public Class getPluginType() {
        return pluginType;
    }

    public Param[] getParams() {
        return params;
    }
}
