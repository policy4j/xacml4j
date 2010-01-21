package com.artagon.xacml.policy;

import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.util.Preconditions;

public class ExplicitFunctionSpecBuilder 
	implements FunctionSpecBuilder
{
	private String functionId;
	private List<ParamSpec> paramSpec;
	private boolean hadVarArg = false;
	
	public ExplicitFunctionSpecBuilder(String functionId){
		Preconditions.checkNotNull(functionId, "Function identifier can't be null");
		this.functionId = functionId;
		this.paramSpec = new LinkedList<ParamSpec>();
	}
	
	public ExplicitFunctionSpecBuilder withParam(ValueType type){
		Preconditions.checkNotNull(type);
		Preconditions.checkState(!hadVarArg, 
				String.format("Can't add parameter after variadic parameter"));
		this.paramSpec.add(new ParamValueTypeSpec(type));
		return this;
	}
	
	public ExplicitFunctionSpecBuilder withAnyBag(){
		this.paramSpec.add(new ParamAnyBagTypeSpec());
		return this;
	}
	
	public ExplicitFunctionSpecBuilder withAnyAttribute(){
		this.paramSpec.add(new ParamAnyAttributeTypeSpec());
		return this;
	}
	
	public ExplicitFunctionSpecBuilder withParam(ValueType type, int min, int max){
		Preconditions.checkNotNull(type);
		Preconditions.checkArgument(min > 0 && max > 0);
		Preconditions.checkArgument(max > min);
		Preconditions.checkArgument(max - min > 1, "Max and min should be different at least by 1");
		hadVarArg = true;
		this.paramSpec.add(new ParamValueTypeSequenceSpec(min, max, type));
		return this;
	}
	
	public FunctionSpec build(RegularFunction function)
	{
		return new RegularFunctionSpec(functionId, function, paramSpec);
	}
	
	public FunctionSpec build(DynamicFunction function)
	{
		return new DynamicFunctionSpec(functionId, function, paramSpec);
	}

}
