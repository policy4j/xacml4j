package org.xacml4j.v30;

/**
 * A XACML policy expression
 *
 * @author Giedrius Trumpickas
 */
public interface Expression
{
	/**
	 * Gets type to which this expression
	 * evaluates to
	 *
	 * @return {@link ValueType}
	 */
	ValueType getEvaluatesTo();

	/**
	 * Evaluates this expression
	 *
	 * @param context an evaluation context
	 * @return {@link Expression} an expression
	 * representing evaluation result, usually evaluation result
	 * is an instance {@link ValueExpression} but in some cases
	 * expression evaluates to itself
	 * @throws EvaluationException if an evaluation error
	 * occurs
	 */
	Expression evaluate(
			EvaluationContext context) throws EvaluationException;

	/**
	 * Accepts {@link ExpressionVisitor} implementation
	 *
	 * @param v
	 * @exception ClassCastException if given implementation
	 * is not supported by this node
	 */
	void accept(ExpressionVisitor v);
}
