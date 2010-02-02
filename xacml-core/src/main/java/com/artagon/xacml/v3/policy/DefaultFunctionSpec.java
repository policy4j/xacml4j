package com.artagon.xacml.v3.policy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.function.FunctionInvocation;
import com.artagon.xacml.v3.policy.function.FunctionReturnTypeResolver;


/**
 * A XACML function specification
 *
 * @param <ReturnType>
 */
public final class DefaultFunctionSpec extends XacmlObject implements FunctionSpec
{
	private final static Logger log = LoggerFactory.getLogger(DefaultFunctionSpec.class);
	
	private String functionId;
	private List<ParamSpec> parameters = new LinkedList<ParamSpec>();
	private boolean lazyParamEval = false;
	
	private FunctionInvocation invocation;
	private FunctionReturnTypeResolver resolver;
	
	public DefaultFunctionSpec(
			String functionId, 
			List<ParamSpec> params, 
			FunctionReturnTypeResolver resolver,
			FunctionInvocation invocation,
			boolean lazyParamEval){
		Preconditions.checkNotNull(functionId);
		Preconditions.checkNotNull(params);
		Preconditions.checkNotNull(invocation);
		Preconditions.checkNotNull(resolver);
		this.functionId = functionId;
		this.parameters.addAll(params);
		this.resolver = resolver;
		this.invocation = invocation;
		this.lazyParamEval = lazyParamEval;
	}
	
	@Override
	public  String getId(){
		return functionId;
	}
	
	
	@Override
	public  List<ParamSpec> getParamSpecs(){
		return parameters;
	}
	
	@Override
	public boolean isRequiresLazyParamEval() {
		return lazyParamEval;
	}

	
	@Override
	public boolean isVariadic(){
		return parameters.isEmpty()?false:parameters.get(parameters.size() - 1).isVariadic();
	}
	
	@Override
	public  int getNumberOfParams(){
		return parameters.size();
	}
	
	@Override
	public ValueType resolveReturnType(Expression... arguments) {
		return resolver.resolve(this, arguments);
	}

	@Override
	public <T extends Value> T invoke(EvaluationContext context,
			Expression... params) throws FunctionInvocationException {
		if(context.isValidateFuncParamAtRuntime()){
			if(!validateParameters(params)){
				throw new FunctionInvocationException(
						"Failed to validate function=\"%s\" parameters", getId());
			}
		}
		try{
			return invocation.invoke(this, context, 
					isRequiresLazyParamEval()?params:evaluate(context, params));
		}catch(EvaluationException e){
			throw new FunctionInvocationException(e, 
					"Failed to invoke function=\"%s\"", getId());
		}
	}

	@Override
	public boolean validateParameters(Expression ... params)
	{
		log.debug("Validating function=\"{}\" parameters", getId());
		boolean result = true;
		ListIterator<ParamSpec> it = parameters.listIterator();
		ListIterator<Expression> expIt = Arrays.asList(params).listIterator();
		while(it.hasNext())
		{
			ParamSpec p = it.next();
			if(!p.validate(expIt)){
				return false;
			}
			if(!it.hasNext() && 
					expIt.hasNext()){
				return false;
			}
		}
		return result?validateAdditional(params):result;
	}
	
	/**
	 * Evaluates given array of function parameters
	 * 
	 * @param context an evaluation context
	 * @param params a function invocation 
	 * parameters
	 * @return an array of evaluated parameters
	 * @throws EvaluationException if an evaluation
	 * error occurs
	 */
	private  Expression[] evaluate(EvaluationContext context, Expression ...params) 
		throws EvaluationException
	{
		Expression[] eval = new Expression[params.length];
		for(int i =0; i < params.length; i++){
			eval[i] = params[i].evaluate(context);
		}
		return eval;
	}
	
	/**
	 * Additional function parameter validation function
	 * 
	 * @param paramIndex a parameter index
	 * in a function signature
	 * @param spec a parameter specification
	 * @param p an actual parameter
	 * @param params an array of all parameters
	 * @return <code>true</code> if a given parameter is valid
	 * according specification
	 */
	protected boolean validateAdditional(Expression ... params){
		return true;
	}
}
