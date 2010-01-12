package com.artagon.xacml.policy.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.DataTypeId;
import com.artagon.xacml.FunctionId;
import com.artagon.xacml.policy.AttributeDataType;
import com.artagon.xacml.policy.DataTypeFactory;
import com.artagon.xacml.policy.FunctionFactory;
import com.artagon.xacml.policy.BaseFunctionSpec;
import com.artagon.xacml.util.Preconditions;

public class BaseFunctionFacatory implements FunctionFactory
{
	private Map<FunctionId, BaseFunctionSpec> functions;
	
	private DataTypeFactory typeRegistry;
	
	public BaseFunctionFacatory(DataTypeFactory types)
	{
		Preconditions.checkNotNull(types);
		this.functions = new ConcurrentHashMap<FunctionId, BaseFunctionSpec>();
		this.typeRegistry = types;
	}
	
	protected final <T extends AttributeDataType> T getDataType(DataTypeId typeId){
		return typeRegistry.getDataType(typeId);
	}
	
	protected final void add(BaseFunctionSpec spec){
		this.functions.put(spec.getId(), spec);
	}

	@Override
	public final BaseFunctionSpec getFunction(FunctionId functionId) {
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
