package com.artagon.xacml.policy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.util.Preconditions;


/**
 * A XACML function specification
 *
 * @param <ReturnType>
 */
public abstract class BaseFunctionSpec implements FunctionSpec
{
	private FunctionId functionId;
	private List<ParamSpec> parameters = new LinkedList<ParamSpec>();
	
	/**
	 * Constructs function specification with a given
	 * function identifier, return type and parameter
	 * descriptors.
	 * 
	 * @param id a function identifier
	 * @param returnType a function return type
	 * @param params a function parameter descriptors
	 */
	public BaseFunctionSpec(FunctionId id, List<ParamSpec> params)
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(params);
		this.functionId = id;
		this.parameters.addAll(params);
	}
	
	@Override
	public  final FunctionId getId(){
		return functionId;
	}
	
	
	@Override
	public  final List<ParamSpec> getParamSpecs(){
		return parameters;
	}
	
	@Override
	public final int getNumberOfParams(){
		return parameters.size();
	}
		
	@Override
	public final Apply createApply(Expression... arguments) {
		Preconditions.checkArgument(validateParameters(arguments));
		return new Apply(this, getReturnType(arguments), arguments);
	}

	@Override
	public final Apply createApply(List<Expression> arguments) {
		Preconditions.checkArgument(validateParameters(arguments));
		return new Apply(this, getReturnType(arguments), arguments);
	}

	/* (non-Javadoc)
	 * @see com.artagon.xacml.policy.FunctionSpec#validateParameters(com.artagon.xacml.policy.Expression)
	 */
	public final boolean validateParameters(Expression... expressions) {
		return validateParameters(Arrays.asList(expressions));
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.policy.FunctionSpec#validateParameters(java.util.List)
	 */
	public final boolean validateParameters(List<Expression> params)
	{
		boolean result = true;
		ListIterator<ParamSpec> it = parameters.listIterator();
		ListIterator<Expression> expIt = params.listIterator();
		while(it.hasNext()){
			ParamSpec p = it.next();
			if(!p.validate(expIt)){
				return false;
			}
			if(!it.hasNext() && 
					expIt.hasNext()){
				return false;
			}
		}
		return result;
	}
	
	protected boolean validate(ParamSpec spec, Expression p, List<Expression> params){
		return true;
	}
	
	protected abstract ValueType getReturnType(List<Expression> arguments);
	protected abstract ValueType getReturnType(Expression ... arguments);
}
