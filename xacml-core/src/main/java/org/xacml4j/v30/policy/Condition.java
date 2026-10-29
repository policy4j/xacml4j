package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.MoreObjects;
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
	 * The occurrences in the predicate of expressions whose values a policy may share in a decision
	 * (design D45); a condition without one never asks its context for the policy. A subclass has none:
	 * it may answer {@link #getExpression()} with another expression than the one it evaluates (review
	 * R-D45-sol)
	 */
	private final int shareableOccurrences;

	/**
	 * Constructs condition with an predicate
	 * expression
	 *
	 * @param predicate an expression which always evaluates
	 * to {@link BooleanVal}
	 */
	public Condition(Expression predicate)
	{
		Preconditions.checkNotNull(predicate, "Condition predicate can not be null");
		final ValueExpTypeInfo resultType = predicate.getEvaluatesTo();
		Preconditions.checkArgument(XacmlTypes.BOOLEAN.equals(resultType),
				"Condition expects an expression " +
					"with=\"%s\" return value, but got expression " +
					"with return value type=\"%s\"",
					XacmlTypes.BOOLEAN, resultType);
		this.predicate = predicate;
		this.shareableOccurrences = getClass() == Condition.class ? PreparedConditions.occurrences(predicate) : 0;
	}

	public static Condition condition(Expression e){
		return new Condition(e);
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
	 * Evaluates this condition and returns defaultProvider of
	 * {@link ConditionResult}
	 *
	 * @param context an evaluation context
	 * @return {@link ConditionResult}
	 */
	public ConditionResult evaluate(EvaluationContext context)
	{
		try
		{
			BooleanVal result = predicateIn(context).evaluate(context);
			return result.get() ? ConditionResult.TRUE : ConditionResult.FALSE;
		}catch(EvaluationException e){
			context.setEvaluationStatus(e.getStatus());
			return ConditionResult.INDETERMINATE;
		}catch(Exception e){
			context.setEvaluationStatus(context.getCurrentRule(),
			                            Status.processingError(e)
			                                  .build());
			return ConditionResult.INDETERMINATE;
		}
	}

	/**
	 * The predicate to evaluate: the one that the policy of the context prepared to share the values of
	 * the expressions its rules repeat (design D45), or else this condition's own
	 */
	private Expression predicateIn(EvaluationContext context)
	{
		return shareableOccurrences > 0 && context.getCurrentPolicy() instanceof Policy policy && policy.mayShareConditions()
				? PreparedConditions.predicate(context, policy, this).orElse(predicate) : predicate;
	}

	/** The occurrences of expressions whose values a policy may share, for its flag (design D45) */
	int shareableOccurrences(){
		return shareableOccurrences;
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public int hashCode(){
		return predicate.hashCode();
	}

	@Override
	public String toString(){
		return MoreObjects
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
