package com.artagon.xacml.v3.spi.function;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.google.common.base.Preconditions;

public class AggregatingFunctionProvider implements FunctionProvider
{
	private Map<String, FunctionProvider> functions;
	
	public AggregatingFunctionProvider(){
		this.functions = new ConcurrentHashMap<String, FunctionProvider>();
	}
	
	public void add(FunctionProvider provider)
	{
		Preconditions.checkNotNull(provider);
		for(String functionId : provider.getProvidedFunctions()){
			if(functions.containsKey(functionId)){
				throw new IllegalArgumentException(String.format("Function provider " +
						"already contains a function with functionId=\"%s\"", 
						functionId));
			}
			FunctionSpec spec = provider.getFunction(functionId);
			Preconditions.checkArgument(spec != null);
			this.functions.put(functionId, provider);
		}
	}
	
	@Override
	public FunctionSpec getFunction(String functionId) {
		FunctionProvider provider = functions.get(functionId);
		if(provider != null){
			return provider.getFunction(functionId);
		}
		return null;
	}

	@Override
	public Iterable<String> getProvidedFunctions() {
		return  Collections.unmodifiableCollection(functions.keySet());
	}

	@Override
	public boolean isFunctionProvided(String functionId) {
		return functions.containsKey(functionId);
	}
	
}
