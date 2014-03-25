package org.xacml4j.v30.pdp;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Condition represents a Boolean expression that refines the applicability
 * of the rule beyond the predicates implied by its target.
 * Therefore, it may be absent in the {@link Rule}
 *
 * @author Giedrius Trumpickas
 */
public class Condition implements PolicyElement
{
	private final Expression predicate;

	/**
	 * Constructs condition with an predicate
	 * expression
	 *
	 * @param predicate an expression which always evaluates
	 * to {@link BooleanExp}
	 */
	public Condition(Expression predicate)
	{
		Preconditions.checkNotNull(predicate, "Condition predicate can not be null");
		final ValueType resultType = predicate.getEvaluatesTo();
		Preconditions.checkArgument(resultType.equals(XacmlTypes.BOOLEAN),
				"Condition expects an expression " +
					"with=\"%s\" return value, but got expression " +
					"with return value type=\"%s\"",
					XacmlTypes.BOOLEAN, resultType);
		this.predicate = predicate;
	}

	/**
	 * Gets condition expression predicate
	 *
	 * @return {@link Expression} a condition
	 * expression predicate
	 */
	public Expression getExpression(){
		return predicate;
	}

	/**
	 * Evaluates this condition and returns instance of
	 * {@link ConditionResult}
	 *
	 * @param context an evaluation context
	 * @return {@link ConditionResult}
	 */
	public ConditionResult evaluate(EvaluationContext context)
	{
		try
		{
			BooleanExp result = (BooleanExp)predicate.evaluate(context);
			return result.getValue()?ConditionResult.TRUE:ConditionResult.FALSE;
		}catch(EvaluationException e){
			context.setEvaluationStatus(e.getStatusCode());
			return ConditionResult.INDETERMINATE;
		}catch(Exception e){
			context.setEvaluationStatus(StatusCode.createProcessingError());
			return ConditionResult.INDETERMINATE;
		}
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public int hashCode(){
		return predicate.hashCode();
	}

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("predicate", predicate)
				.toString();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof Condition)){
			return false;
		}
		Condition c = (Condition)o;
		return predicate.equals(c.predicate);
	}
}
