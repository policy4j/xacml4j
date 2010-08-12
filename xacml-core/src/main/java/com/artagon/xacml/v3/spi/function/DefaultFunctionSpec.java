package com.artagon.xacml.v3.spi.function;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionInvocationException;
import com.artagon.xacml.v3.FunctionParamSpec;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.ValueExpression;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.google.common.base.Preconditions;

/**
 * A XACML function specification implementation
 *
 * @param <ReturnType>
 */
final class DefaultFunctionSpec extends XacmlObject implements FunctionSpec
{	
	private final static Logger log = LoggerFactory.getLogger(DefaultFunctionSpec.class);
	
	private String functionId;
	private String legacyId;
	private List<FunctionParamSpec> parameters = new LinkedList<FunctionParamSpec>();
	private boolean lazyParamEval = false;
	
	private FunctionInvocation invocation;
	private FunctionReturnTypeResolver resolver;
	private FunctionParametersValidator validator;
	
	/**
	 * Constructs function spec with given function
	 * identifier and parameters
	 * 
	 * @param functionId a function identifier
	 * @param legacyId a legacy identifier
	 * @param params a function parameters spec
	 * @param resolver a function return type resolver
	 * @param invocation a function implementation
	 * @param lazyParamEval a flag indicating
	 * if function parameters needs to be evaluated
	 * before passing them to the function
	 */
	public DefaultFunctionSpec(
			String functionId, 
			String legacyId,
			List<FunctionParamSpec> params, 
			FunctionReturnTypeResolver resolver,
			FunctionInvocation invocation,
			FunctionParametersValidator validator,
			boolean lazyParamEval){
		Preconditions.checkNotNull(functionId);
		Preconditions.checkNotNull(params);
		Preconditions.checkNotNull(invocation);
		Preconditions.checkNotNull(resolver);
		this.functionId = functionId;
		this.parameters.addAll(params);
		this.resolver = resolver;
		this.validator = validator;
		this.invocation = invocation;
		this.lazyParamEval = lazyParamEval;
		this.legacyId = legacyId;
	}
	
	public DefaultFunctionSpec(
			String functionId, 
			String legacyId,
			List<FunctionParamSpec> params, 
			FunctionReturnTypeResolver resolver,
			FunctionInvocation invocation,
			boolean lazyParamEval){
		this(functionId, legacyId, params, resolver, invocation, null, lazyParamEval);
	}
	
	public DefaultFunctionSpec(
			String functionId, 
			List<FunctionParamSpec> params, 
			FunctionReturnTypeResolver resolver,
			FunctionInvocation invocation,
			boolean lazyParamEval){
		this(functionId, null, params, resolver, invocation, null, lazyParamEval);
	}
	
	@Override
	public  String getId(){
		return functionId;
	}
	
	
	
	@Override
	public String getLegacyId() {
		return legacyId;
	}

	@Override
	public final FunctionParamSpec getParamSpecAt(int index){
		return parameters.get(index);
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

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ValueExpression> T invoke(EvaluationContext context,
			Expression... params) throws EvaluationException {
		
		try
		{
			if(context.isValidateFuncParamsAtRuntime()){
				validateParameters(params);
			}
			T result = (T)invocation.invoke(this, context, 
					isRequiresLazyParamEval()?params:evaluate(context, params));
			if(log.isDebugEnabled()){
				log.debug("Function=\"{}\" " +
						"invocation result=\"{}\"", getId(), result);
			}
			return result;
		}
		catch(EvaluationException e){
			throw e;
		}
		catch(Exception e){
			throw new FunctionInvocationException(context, this, e, 
					"Failed to invoke function=\"%s\"", getId());
		}
	}

	@Override
	public void validateParametersAndThrow(Expression ... params) throws XacmlSyntaxException
	{
		ListIterator<FunctionParamSpec> it = parameters.listIterator();
		ListIterator<Expression> expIt = Arrays.asList(params).listIterator();
		while(it.hasNext())
		{
			FunctionParamSpec p = it.next();
			if(!p.validate(expIt)){
				throw new XacmlSyntaxException(
						"Expression at index=\"%d\", " +
						"can't be used as function=\"%s\" parameter", 
						expIt.nextIndex() - 1, functionId);
			}
			if(!it.hasNext() && 
					expIt.hasNext()){
				throw new XacmlSyntaxException(
						"Expression at index=\"%d\", " +
						"can't be used as function=\"%s\" parameter", 
						expIt.nextIndex() - 1, functionId);
			}
		}
		if(!validateAdditional(params)){
			throw new XacmlSyntaxException("Failed addition validation");
		}
	}
	
	@Override
	public boolean validateParameters(Expression ... params)
	{
		ListIterator<FunctionParamSpec> it = parameters.listIterator();
		ListIterator<Expression> expIt = Arrays.asList(params).listIterator();
		while(it.hasNext())
		{
			FunctionParamSpec p = it.next();
			if(!p.validate(expIt)){
				return false;
			}
			if(!it.hasNext() && 
					expIt.hasNext()){
				return false;
			}
		}
		return validateAdditional(params);
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
	private boolean validateAdditional(Expression ... params){
		return (validator == null)?true:validator.validate(this, params);
	}	
}
