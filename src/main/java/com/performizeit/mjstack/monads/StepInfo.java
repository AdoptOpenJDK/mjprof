package com.performizeit.mjstack.monads;

public class StepInfo {
	private Class clazz;
	private Class[] paramTypes;
	private String description;
	
	public StepInfo(Class name, Class[] paramTypes,String description) {
		this.clazz=name;
		this.paramTypes=paramTypes;
		this.description=description;
	}

	public Class getClazz() {
		return clazz;
	}

	public Class[] getParamTypes() {
		return paramTypes;
	}

	public String getDescription() {
		return description;
	}
	
	public int getArgNum(){
		return paramTypes.length;
	}
	
	@Override
	public String toString() {
		return "name "+ clazz + " description " + description + " param " + paramTypes;
	}

}
