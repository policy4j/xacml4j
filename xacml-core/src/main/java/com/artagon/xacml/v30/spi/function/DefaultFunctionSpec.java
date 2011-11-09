package com.artagon.xacml.v30.spi.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.pdp.EvaluationException;
import com.artagon.xacml.v30.pdp.Expression;
import com.artagon.xacml.v30.pdp.FunctionInvocationException;
import com.artagon.xacml.v30.pdp.FunctionParamSpec;
import com.artagon.xacml.v30.pdp.FunctionSpec;
import com.artagon.xacml.v30.pdp.ValueExpression;
import com.artagon.xacml.v30.pdp.ValueType;
import com.artagon.xacml.v30.pdp.XacmlObject;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;
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
	private boolean evaluateParameters = false;
	
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
	 * @param evaluateParameters a flag indicating
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
			boolean evaluateParameters){
		Preconditions.checkNotNull(functionId);
		Preconditions.checkNotNull(params);
		Preconditions.checkNotNull(invocation);
		Preconditions.checkNotNull(resolver);
		this.functionId = functionId;
		this.parameters.addAll(params);
		this.resolver = resolver;
		this.validator = validator;
		this.invocation = invocation;
		this.evaluateParameters = evaluateParameters;
		this.legacyId = legacyId;
	}
	
	public DefaultFunctionSpec(
			String functionId, 
			String legacyId,
			List<FunctionParamSpec> params, 
			FunctionReturnTypeResolver resolver,
			FunctionInvocation invocation,
			boolean evaluateParameters){
		this(functionId, legacyId, params, resolver, invocation, null, evaluateParameters);
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
		return evaluateParameters;
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
	public ValueType resolveReturnType(List<Expression> arguments) {
		return resolver.resolve(this, arguments);
	}
	
	public <T extends ValueExpression> T invoke(EvaluationContext context,
			Expression ...arguments) throws EvaluationException {
		return this.<T>invoke(context, Arrays.asList(arguments));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ValueExpression> T invoke(EvaluationContext context,
			List<Expression> arguments) throws EvaluationException {
		
		try
		{
			if(context.isValidateFuncParamsAtRuntime()){
				if(log.isDebugEnabled()){
					log.debug("Validating " +
							"function=\"{}\" parameters", functionId);
				}
				validateParameters(arguments);
			}
			T result = (T)invocation.invoke(this, context, 
					isRequiresLazyParamEval()?arguments:evaluate(context, arguments));
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
	public void validateParametersAndThrow(List<Expression> arguments) throws XacmlSyntaxException
	{
		ListIterator<FunctionParamSpec> it = parameters.listIterator();
		ListIterator<Expression> expIt = arguments.listIterator();
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
		if(!validateAdditional(arguments)){
			throw new XacmlSyntaxException("Failed addition validation");
		}
	}
	
	@Override
	public boolean validateParameters(List<Expression> arguments)
	{
		ListIterator<FunctionParamSpec> it = parameters.listIterator();
		ListIterator<Expression> expIt = arguments.listIterator();
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
		return validateAdditional(arguments);
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
	private List<Expression> evaluate(EvaluationContext context, List<Expression> arguments) 
		throws EvaluationException
	{
		List<Expression> eval = new ArrayList<Expression>(arguments.size());
		for(Expression exp : arguments){
			eval.add(exp.evaluate(context));
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
	private boolean validateAdditional(List<Expression> arguments){
		return (validator == null)?true:validator.validate(this, arguments);
	}	
}
