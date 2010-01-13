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
abstract class BaseFunctionSpec implements FunctionSpec
{
	private FunctionId functionId;
	private List<ParamSpec> parameters = new LinkedList<ParamSpec>();
	
	public BaseFunctionSpec(FunctionId id, List<ParamSpec> params){
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
		return new Apply(this, resolveReturnType(arguments), arguments);
	}
	
	@Override
	public final FunctionReferenceExpression createReference() {
		ValueType returnType = getReturnType();
		if(returnType == null){
			throw new UnsupportedOperationException(
					String.format(
							"Can't create reference to=\"%s\" " +
							"function with a dynamic return type", functionId));
		}
		return new FunctionReferenceExpression(this, returnType);
	}

	public final boolean validateParameters(Expression... expressions) {
		return validateParameters(Arrays.asList(expressions));
	}
	
	protected final ValueType getReturnType(Expression ... arguments){
		return resolveReturnType(Arrays.asList(arguments));
	}
	
	@Override
	public final Value invoke(EvaluationContext context, Expression... expressions) 
		throws PolicyEvaluationException{
		return invoke(context, Arrays.asList(expressions));
	}
	
	public final boolean validateParameters(List<Expression> params){
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
	
	/**
	 * Tries to get function return type statically. 
	 * If return type is not statically defined method
	 * returns <code>null</code> otherwise {@link ValueType}
	 * 
	 * @return {@link ValueType} or <code>null</code>
	 */
	protected abstract ValueType getReturnType();
	
	/**
	 * Resolves function return type based on function
	 * invocation arguments
	 * 
	 * @param arguments a function invocation arguments
	 * @return {@link ValueType} resolved function return type
	 */
	protected abstract ValueType resolveReturnType(List<Expression> arguments);
}
