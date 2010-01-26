package com.artagon.xacml.v3.policy.function;

import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.ParamSpec;
import com.artagon.xacml.v3.policy.ValueType;

public class DefaultFunctionSpecBuilder 
	implements FunctionSpecBuilder
{
	private String functionId;
	private List<ParamSpec> paramSpec;
	private boolean hadVarArg = false;
	
	public DefaultFunctionSpecBuilder(String functionId){
		Preconditions.checkNotNull(functionId, "Function identifier can't be null");
		this.functionId = functionId;
		this.paramSpec = new LinkedList<ParamSpec>();
	}
	
	public DefaultFunctionSpecBuilder withParam(ValueType type){
		Preconditions.checkNotNull(type);
		Preconditions.checkState(!hadVarArg, 
				String.format("Can't add parameter after variadic parameter"));
		this.paramSpec.add(new ParamValueTypeSpec(type));
		return this;
	}
	
	public DefaultFunctionSpecBuilder withParam(ValueType type, int min, int max){
		Preconditions.checkNotNull(type);
		Preconditions.checkArgument(min >= 0 && max > 0);
		Preconditions.checkArgument(max > min);
		Preconditions.checkArgument(max - min > 1, "Max and min should be different at least by 1");
		hadVarArg = true;
		this.paramSpec.add(new ParamValueTypeSequenceSpec(min, max, type));
		return this;
	}
	
	public FunctionSpec build(StaticallyTypedFunction function)
	{
		return new StaticallyTypedFunctionSpec(functionId, function, paramSpec);
	}
	
	public FunctionSpec build(DynamicallyTypedFunction function)
	{
		return new DynamicallyTypedFunctionSpec(functionId, function, paramSpec);
	}

}
