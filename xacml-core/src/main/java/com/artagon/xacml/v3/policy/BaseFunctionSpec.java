package com.artagon.xacml.v3.policy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;


/**
 * A XACML function specification
 *
 * @param <ReturnType>
 */
public abstract class BaseFunctionSpec extends XacmlObject implements FunctionSpec
{
	private final static Logger log = LoggerFactory.getLogger(BaseFunctionSpec.class);
	
	private String functionId;
	private List<ParamSpec> parameters = new LinkedList<ParamSpec>();
	private boolean lazyParamEval = false;
	
	protected BaseFunctionSpec(String functionId, 
			List<ParamSpec> params, 
			boolean lazyParamEval){
		Preconditions.checkNotNull(functionId);
		Preconditions.checkNotNull(params);
		this.functionId = functionId;
		this.parameters.addAll(params);
		this.lazyParamEval = lazyParamEval;
	}
	
	@Override
	public  final String getId(){
		return functionId;
	}
	
	
	@Override
	public  final List<ParamSpec> getParamSpecs(){
		return parameters;
	}
	
	@Override
	public final boolean isRequiresLazyParamEval() {
		return lazyParamEval;
	}

	
	@Override
	public boolean isVariadic(){
		return parameters.isEmpty()?false:parameters.get(parameters.size() - 1).isVariadic();
	}
	
	@Override
	public final int getNumberOfParams(){
		return parameters.size();
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Value> T invoke(EvaluationContext context,
			Expression... params) throws EvaluationException {
		if(context.isValidateFuncParamAtRuntime()){
			if(!validateParameters(params)){
				throw new EvaluationException(
						"Function=\"%s\" can't be invoked with a given parameters", getId());
			}
		}
		return (T)doInvoke(context, 
				isRequiresLazyParamEval()?params:evaluate(context, params));
	}
	
	/**
	 * Actual function implementation
	 * 
	 * @param <T>
	 * @param context an evaluation context
	 * @param params a function invocation 
	 * parameters
	 * @return <T> a function invocation result
	 * @throws EvaluationException if function
	 * invocation fails
	 */
	protected abstract <T extends Value> T doInvoke(
			EvaluationContext context, Expression ...params) throws EvaluationException;

	@Override
	public final boolean validateParameters(Expression ... params)
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
	protected final Expression[] evaluate(EvaluationContext context, Expression ...params) 
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
