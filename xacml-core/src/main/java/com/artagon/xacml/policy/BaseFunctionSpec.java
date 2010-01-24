package com.artagon.xacml.policy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.artagon.xacml.util.Preconditions;


/**
 * A XACML function specification
 *
 * @param <ReturnType>
 */
public abstract class BaseFunctionSpec implements FunctionSpec
{
	private String functionId;
	private List<ParamSpec> parameters = new LinkedList<ParamSpec>();
	
	protected BaseFunctionSpec(String functionId, List<ParamSpec> params){
		Preconditions.checkNotNull(functionId);
		Preconditions.checkNotNull(params);
		this.functionId = functionId;
		this.parameters.addAll(params);
	}
	
	@Override
	public  final String getXacmlId(){
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
		validateParameters(arguments);
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
	
	public final boolean validateParameters(Expression ... params){
		boolean result = true;
		ListIterator<ParamSpec> it = parameters.listIterator();
		ListIterator<Expression> expIt = Arrays.asList(params).listIterator();
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
	
	protected boolean validate(ParamSpec spec, Expression p, Expression ... params){
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
	protected abstract ValueType resolveReturnType(Expression ... arguments);
}
