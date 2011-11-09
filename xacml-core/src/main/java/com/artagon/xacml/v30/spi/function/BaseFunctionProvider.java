package com.artagon.xacml.v30.spi.function;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.artagon.xacml.v30.pdp.FunctionSpec;
import com.google.common.base.Preconditions;

public class BaseFunctionProvider implements FunctionProvider
{
	private ConcurrentMap<String, FunctionSpec> functions;
	
	protected BaseFunctionProvider(){
		this.functions = new ConcurrentHashMap<String, FunctionSpec>();
	}
	
	/**
	 * Adds given {@link DefaultFunctionSpec} to this factory
	 * 
	 * @param spec a function specification
	 * @exception 
	 */
	protected final void add(FunctionSpec spec)
	{
		Preconditions.checkNotNull(spec);
		FunctionSpec other = functions.putIfAbsent(spec.getId(), spec);
		Preconditions.checkState(other == null,
					"This factory already contains " +
					"function=\"%s\" with a given identifier=\"%s\"", 
					spec, spec.getId());
		if(spec.getLegacyId() != null){
			other = functions.putIfAbsent(spec.getLegacyId(), spec);
			Preconditions.checkState(other == null,
					"This factory already contains " +
					"function=\"%s\" with a given identifier=\"%s\"", 
					spec, spec.getId());
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
