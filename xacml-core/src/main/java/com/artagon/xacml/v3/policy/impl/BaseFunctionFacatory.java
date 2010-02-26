package com.artagon.xacml.v3.policy.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.FunctionFactory;
import com.artagon.xacml.v3.policy.FunctionSpec;

public class BaseFunctionFacatory implements FunctionFactory
{
	private Map<String, FunctionSpec> functions;
	
	protected BaseFunctionFacatory(){
		this.functions = new ConcurrentHashMap<String, FunctionSpec>();
	}
	
	/**
	 * Adds given {@link FunctionSpec} to this factory
	 * 
	 * @param spec a function specification
	 * @exception 
	 */
	protected final void add(FunctionSpec spec){
		FunctionSpec other = functions.get(spec.getId());
		Preconditions.checkArgument(other == null, 
				String.format("This factory already contains function=\"%s\" with a given identifier=\"%s\"", 
						spec, spec.getId()));
		functions.put(spec.getId(), spec);
	}

	@Override
	public final FunctionSpec getFunction(String functionId) {
		return functions.get(functionId);
	}

	@Override
	public Iterable<String> getSupportedFunctions() {
		return functions.keySet();
	}

	@Override
	public boolean isSupported(String functionId) {
		return functions.containsKey(functionId);
	}

}
