package com.artagon.xacml.v3.policy.spi.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.UnsupportedFunctionException;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;
import com.artagon.xacml.v3.policy.spi.FunctionProvidersRegistry;

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
		throws UnsupportedFunctionException
	{
		FunctionProvider provider = providers.get(functionId);
		if(provider == null){
			throw new UnsupportedFunctionException(functionId);
		}
		FunctionSpec spec = provider.getFunction(functionId);
		Preconditions.checkState(spec != null);
		return spec;
	}
}
