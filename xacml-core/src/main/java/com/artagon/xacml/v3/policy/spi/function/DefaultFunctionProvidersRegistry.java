package com.artagon.xacml.v3.policy.spi.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;
import com.artagon.xacml.v3.policy.spi.FunctionProvidersRegistry;
import com.google.common.base.Preconditions;

public class DefaultFunctionProvidersRegistry implements FunctionProvidersRegistry 
{
	private Map<String, FunctionProvider> providers;
	
	public DefaultFunctionProvidersRegistry(){
		this.providers = new ConcurrentHashMap<String, FunctionProvider>();
	}
	
	public void add(FunctionProvider provider)
	{
		for(String functionId : provider.getProvidedFunctions()){
			providers.put(functionId, provider);
		}
	}
	
	public void addAll(Iterable<FunctionProvider> providers){
		for(FunctionProvider provider : providers){
			add(provider);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.impl.FunctionProvidersRegistry#getFunction(java.lang.String)
	 */
	public FunctionSpec getFunction(String functionId) 
	{
		FunctionProvider provider = providers.get(functionId);
		if(provider == null){
			return null;
		}
		FunctionSpec spec = provider.getFunction(functionId);
		Preconditions.checkState(spec != null);
		return spec;
	}
}
