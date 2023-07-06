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

import java.util.Collection;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.SyntaxException;



public class AdviceExpression extends BaseDecisionRuleResponseExpression
{
	/**
	 * Constructs advice expression with a given identifier
	 * @param b an advice expression builder
	 * @exception SyntaxException
	 */
	private AdviceExpression(Builder b){
		super(b);
	}

	/**
	 * Evaluates this advice expression by evaluating
	 * all {@link AttributeAssignmentExpression}
	 *
	 * @param context an evaluation context
	 * @return {@link Advice} defaultProvider
	 * @throws EvaluationException if an evaluation error
	 * occurs
	 */
	public Advice evaluate(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attributes = evaluateAttributeAssignments(context);
		return Advice
				.builder(getId(), getEffect())
				.attributes(attributes)
				.build();
	}

	public static Builder builder(String id, Effect appliesTo){
		return new Builder().id(id).effect(appliesTo);
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		for(AttributeAssignmentExpression exp : getAttributeAssignmentExpressions()){
			exp.accept(v);
		}
		v.visitLeave(this);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof AdviceExpression)){
			return false;
		}
		AdviceExpression ox = (AdviceExpression)o;
		return id.equals(ox.id)
			&& effect.equals(ox.effect)
			&& attributeExpressions.equals(ox.attributeExpressions);
	}

	public static class Builder extends BaseDecisionRuleResponseExpression.Builder<Builder>
	{

		@Override
		protected Builder getThis() {
			return this;
		}

		public AdviceExpression build(){
			return new AdviceExpression(this);
		}
	}
}
