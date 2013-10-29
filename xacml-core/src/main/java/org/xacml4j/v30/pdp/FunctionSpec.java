package org.xacml4j.v30.pdp;

import java.util.List;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.XacmlSyntaxException;



public interface FunctionSpec
{
	/**
	 * Gets function XACML identifier.
	 *
	 * @return XACML function identifier
	 */
	String getId();

	/**
	 * Gets function legacy identifier
	 * if any available
	 *
	 * @return legacy identifier
	 */
	String getLegacyId();

	/**
	 * Gets parameter specification
	 * at given
	 *
	 * @param index a parameter index
	 * @return {@link FunctionParamSpec} at given
	 * index
	 */
	FunctionParamSpec getParamSpecAt(int index);

	/**
	 * Gets number of function formal
	 * parameters
	 *
	 * @return gets number of function formal
	 * parameters
	 */
	int getNumberOfParams();

	/**
	 * Tells if this function has variable length
	 * parameter
	 *
	 * @return <code>true</code> if it does
	 * <code>false</code> otherwise
	 */
	boolean isVariadic();

	/**
	 * Tests if this function requires lazy
	 * parameters evaluation
	 *
	 * @return <code>true</code> if this
	 * function requires lazy parameters
	 * evaluation
	 */
	boolean isRequiresLazyParamEval();

	/**
	 * Validates given array of expressions
	 * as potential function invocation arguments
	 *
	 * @param arguments an array of expressions
	 */
	boolean validateParameters(List<Expression> arguments);

	/**
	 * Validates given array of expressions
	 * as potential function invocation arguments
	 *
	 * @param arguments an array of expressions
	 * @exception XacmlSyntaxException
	 */
	void validateParametersAndThrow(List<Expression> arguments)
		throws XacmlSyntaxException;

	/**
	 * Resolves function return type based on function
	 * invocation arguments
	 *
	 * @param arguments a function invocation arguments
	 * @return {@link ValueType} resolved function return type
	 */
	ValueType resolveReturnType(List<Expression> arguments);

	/**
	 * Invokes this function with a given arguments
	 *
	 * @return {@link ValueExpression} instance representing
	 * function invocation result
	 */
	<T extends ValueExpression> T invoke(EvaluationContext context, List<Expression> arguments)
		throws EvaluationException;

	/**
	 * Invokes this function with a given arguments
	 *
	 * @return {@link ValueExpression} instance representing
	 * function invocation result
	 */
	<T extends ValueExpression> T invoke(EvaluationContext context, Expression ...arguments)
		throws EvaluationException;
}
