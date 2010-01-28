package com.artagon.xacml.v3.policy.function;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.Value;

public class ReflectionBasedFunctionInvocation implements FunctionInvocation
{
	private final static Logger log = LoggerFactory.getLogger(ReflectionBasedFunctionInvocation.class);
	
	private Object factoryInstance;
	private Method function;
	private boolean evalContextRequired;
	
	public ReflectionBasedFunctionInvocation(Object factoryInstance, 
			Method m, boolean evalContextRequired)
	{
		Preconditions.checkNotNull(m);
		this.factoryInstance = factoryInstance;
		this.function = m;
		this.evalContextRequired = evalContextRequired;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Value> T invoke(FunctionSpec spec,
			EvaluationContext context, Expression... arguments)
			throws EvaluationException 
	{
		if(log.isDebugEnabled()){
			log.debug("FunctionSpec=\"{}\"", spec);
		}
		Preconditions.checkState(!(spec.isVariadic() ^ function.isVarArgs()),
				"Function=\"%s\" spec says variadic=\"%b\" but method=\"%s\" is declared as vararg=\"%b\"", 
				spec.getId(), spec.isVariadic(), function.getName(), function.isVarArgs());
		try
		{
			Object[] p = arguments;
			int paramStartIndex = 0;
			if(evalContextRequired){
				log.debug("Passing evaluation context as first parameter");
				p[0] = context;
				paramStartIndex++;
			}
			if(spec.isVariadic()){
				log.debug("Function=\"{}\" number of expected params=\"{}\"", spec.getId(), spec.getNumberOfParams());
				log.debug("Function=\"{}\" number of given params=\"{}\" in invocation", spec.getId(), arguments.length);
				p = new Object[spec.getNumberOfParams() + (evalContextRequired?1:0)];
				System.arraycopy(arguments, 0, p, paramStartIndex, spec.getNumberOfParams() - 1); 
				Object[] varArg = null;
				if(spec.getNumberOfParams() < arguments.length - 1){
					varArg = new Object[arguments.length - (spec.getNumberOfParams() - 1)];
					log.debug("VarArg array length=\"{}\"", varArg.length);
					System.arraycopy(arguments, spec.getNumberOfParams() - 1, varArg, 0, varArg.length);
				} 
				p[p.length - 1] = varArg;
			}
			return (T)function.invoke(factoryInstance, p);
		}catch(Exception e){
			log.error("Failed to invoke function=\"{}\"", spec.getId());
			log.error(e.getMessage(), e);
			throw new EvaluationException(e, "Failed to invoke function=\"%s\"", spec.getId());
		}
	}
}
