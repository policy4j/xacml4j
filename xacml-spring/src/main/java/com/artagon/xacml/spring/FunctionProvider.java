package com.artagon.xacml.spring;


public class FunctionProvider
{
	private Class<?> providerClass;
		
	public void setProviderClass(Class<FunctionProvider> providerClazz){
		this.providerClass = providerClazz;
	}
	
	public Class<?> getProviderClass(){
		return providerClass;
	}
}
