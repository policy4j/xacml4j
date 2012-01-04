package com.artagon.xacml.spring;

import com.google.common.base.Preconditions;


public class FunctionProvider
{
	private Class<?> providerClass;
	private Object providerInstance;
		
	public void setProviderClass(Class<FunctionProvider> providerClazz){
		Preconditions.checkState(providerInstance  == null, 
				"Either provider instance or class must be specified but not both");
		this.providerClass = providerClazz;
	}
	
	public void setProviderInstance(Object instance){
		Preconditions.checkState(providerClass  == null, 
				"Either provider instance or class must be specified but not both");
		this.providerInstance = instance;
	}
	
	public Class<?> getProviderClass(){
		return providerClass;
	}
	
	public Object getProviderInstance(){
		return providerInstance;
	}
}
