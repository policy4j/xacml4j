package com.artagon.xacml.policy.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.FunctionSpec;

public class BaseFunctionFacatory implements FunctionFactory
{
	private Map<FunctionId, FunctionSpec> functions;

	
	public BaseFunctionFacatory()
	{
		this.functions = new ConcurrentHashMap<FunctionId, FunctionSpec>();
	}
	
	
	
	protected final void add(FunctionSpec spec){
		this.functions.put(spec.getId(), spec);
	}

	@Override
	public final FunctionSpec getFunction(FunctionId functionId) {
		return functions.get(functionId);
	}

	@Override
	public Iterable<FunctionId> getSupportedFunctions() {
		return functions.keySet();
	}

	@Override
	public boolean isSupported(FunctionId functionId) {
		return functions.containsKey(functionId);
	}

}
