package com.artagon.xacml.v3.policy.spi.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;

public class BaseFunctionProvider implements FunctionProvider
{
	private Map<String, FunctionSpec> functions;
	
	protected BaseFunctionProvider(){
		this.functions = new ConcurrentHashMap<String, FunctionSpec>();
	}
	
	/**
	 * Adds given {@link DefaultFunctionSpec} to this factory
	 * 
	 * @param spec a function specification
	 * @exception 
	 */
	protected final void add(FunctionSpec spec){
		FunctionSpec other = functions.get(spec.getId());
		if(other != null){
			throw new IllegalArgumentException(
					String.format("This factory already contains " +
					"function=\"%s\" with a given identifier=\"%s\"", 
					spec, spec.getId()));
		}
		functions.put(spec.getId(), spec);
		if(spec.getLegacyId() != null){
			functions.put(spec.getLegacyId(), spec);
		}
	}

	@Override
	public final FunctionSpec getFunction(String functionId) {
		return functions.get(functionId);
	}

	@Override
	public final Iterable<String> getProvidedFunctions() {
		return functions.keySet();
	}

	@Override
	public final boolean isFunctionProvided(String functionId) {
		return functions.containsKey(functionId);
	}

}
