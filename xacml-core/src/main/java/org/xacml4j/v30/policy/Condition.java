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
import org.xacml4j.v30.ValueTypeInfo;
import org.xacml4j.v30.types.BooleanValue;
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
	 * Constructs condition with an predicate
	 * expression
	 *
	 * @param predicate an expression which always evaluates
	 * to {@link BooleanValue}
	 */
	public Condition(Expression predicate)
	{
		Preconditions.checkNotNull(predicate, "Condition predicate can not be null");
		final ValueTypeInfo resultType = predicate.getEvaluatesTo();
		Preconditions.checkArgument(XacmlTypes.BOOLEAN.equals(resultType),
				"Condition expects an expression " +
					"with=\"%s\" return value, but got expression " +
					"with return value type=\"%s\"",
					XacmlTypes.BOOLEAN, resultType);
		this.predicate = predicate;
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
			BooleanValue result = predicate.evaluate(context);
			return result.value()?ConditionResult.TRUE:ConditionResult.FALSE;
		}catch(EvaluationException e){
			context.setEvaluationStatus(context.getCurrentRule(),
					e.getStatus());
			return ConditionResult.INDETERMINATE;
		}catch(Exception e){
			context.setEvaluationStatus(context.getCurrentRule(),
			                            Status.processingError(e)
			                                  .build());
			return ConditionResult.INDETERMINATE;
		}
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
