package com.artagon.xacml.v3.policy.impl.function;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionInvocation;
import com.artagon.xacml.v3.policy.FunctionInvocationException;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.Value;

class ReflectionBasedFunctionInvocation implements FunctionInvocation
{
	private final static Logger log = LoggerFactory.getLogger(ReflectionBasedFunctionInvocation.class);
	
	private Object factoryInstance;
	private Method functionMethod;
	private boolean evalContextRequired;
	
	/**
	 * Constructs XACML function invoker
	 * 
	 * @param factoryInstance a method library instance
	 * @param m a XACML function implementation
	 * @param evalContextRequired a flag indicating if method
	 * requires an {@link EvaluationContext} reference
	 */
	ReflectionBasedFunctionInvocation(Object factoryInstance, 
			Method m, boolean evalContextRequired)
	{
		Preconditions.checkNotNull(m);
		Preconditions.checkArgument(factoryInstance == null || 
				!Modifier.isStatic(m.getModifiers()));
		this.factoryInstance = factoryInstance;
		this.functionMethod = m;
		this.evalContextRequired = evalContextRequired;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Value> T invoke(FunctionSpec spec,
			EvaluationContext context, Expression... arguments)
			throws FunctionInvocationException 
	{
		if(log.isDebugEnabled()){
			log.debug("FunctionSpec=\"{}\"", spec);
		}
		Preconditions.checkState(!(spec.isVariadic() ^ functionMethod.isVarArgs()),
				"Function=\"%s\" spec says variadic=\"%b\" but method=\"%s\" is declared as vararg=\"%b\"", 
				spec.getId(), spec.isVariadic(), functionMethod.getName(), functionMethod.isVarArgs());
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
			return (T)functionMethod.invoke(factoryInstance, params);
		}
		catch(Exception e){
			throw new FunctionInvocationException(context, spec, 
					e, "Failed to invoke function=\"%s\"", spec.getId());
		}
	}
}
