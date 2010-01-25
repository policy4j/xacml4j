package com.artagon.xacml.policy.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.policy.FunctionSpec;

public class BaseFunctionFacatory implements FunctionFactory
{
	private Map<String, FunctionSpec> functions;

	
	public BaseFunctionFacatory()
	{
		this.functions = new ConcurrentHashMap<String, FunctionSpec>();
	}
	
	protected final void add(FunctionSpec spec){
		this.functions.put(spec.getXacmlId(), spec);
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
