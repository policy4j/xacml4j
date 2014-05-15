package org.xacml4j.v30.pdp;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;


public class ObligationExpression extends
	BaseDecisionRuleResponseExpression
{
	public ObligationExpression(Builder b)
	{
		super(b);
	}

	public Obligation evaluate(EvaluationContext context) throws EvaluationException{
		return Obligation
				.builder(getId(), getEffect())
				.attributes(evaluateAttributeAssignments(context))
				.build();
	}

	public static Builder builder(String id, Effect appliesTo){
		return new Builder().id(id).effect(appliesTo);
	}

	@Override
	public void accept(PolicyVisitor v) {
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
		if(!(o instanceof ObligationExpression)){
			return false;
		}
		ObligationExpression ox = (ObligationExpression)o;
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

		public ObligationExpression build(){
			return new ObligationExpression(this);
		}
	}

}
