package com.artagon.xacml.v3.policy.spi.function;

import java.lang.reflect.Array;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.FunctionInvocation;
import com.artagon.xacml.v3.policy.FunctionInvocationException;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.Value;

public abstract class AbstractReflectionBasedFunctionInvocation implements FunctionInvocation
{
	private boolean evalContextRequired;
	
	/**
	 * Constructs XACML function invoker
	 * 
	 * @param factoryInstance a method library instance
	 * @param m a XACML function implementation
	 * @param evalContextRequired a flag indicating if method
	 * requires an {@link EvaluationContext} reference
	 */
	AbstractReflectionBasedFunctionInvocation( 
			boolean evalContextRequired)
	{		
		this.evalContextRequired = evalContextRequired;
	}
	
	@Override
	public final <T extends Value> T invoke(FunctionSpec spec,
			EvaluationContext context, Expression... arguments)
			throws FunctionInvocationException 
	{
		try
		{
			Object[] params = new Object[spec.getNumberOfParams() + (evalContextRequired?1:0)];
			int startIndex = 0;
			if(evalContextRequired){
				params[0] = context;
				startIndex++;
			}
			System.arraycopy(arguments, 0, params, startIndex, 
					spec.isVariadic()?spec.getNumberOfParams() - 1:spec.getNumberOfParams());
			if(spec.isVariadic()){ 
				Object varArgArray = null;
				if(spec.getNumberOfParams() <= arguments.length){
					int size = arguments.length - (spec.getNumberOfParams() - 1);
					varArgArray = Array.newInstance(arguments[spec.getNumberOfParams() - 1].getClass(), size);
					System.arraycopy(arguments, spec.getNumberOfParams() - 1, varArgArray, 0, size);
				}
				params[params.length - 1] = varArgArray;
			}
			return invoke(params);
		}
		catch(Exception e){
			throw new FunctionInvocationException(context, spec, 
					e, "Failed to invoke function=\"%s\"", spec.getId());
		}
	}
	
	protected abstract <T extends Value> T invoke(Object ...params) throws Exception;
}
