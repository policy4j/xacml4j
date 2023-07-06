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

import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.PolicyElement;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.ValueExp;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;

/**
 * A base class for XACML Obligation or Advice expressions
 *
 * @author Giedrius Trumpickas
 */
abstract class BaseDecisionRuleResponseExpression implements PolicyElement
{
	protected final String id;
	protected final Effect effect;
	protected final Multimap<String, AttributeAssignmentExpression> attributeExpressions;

	private int hashCode;

	/**
	 * Constructs expression from a given expression builder
	 *
	 * @param b {@link BaseDecisionRuleResponseExpression} builder defaultProvider
	 */
	protected BaseDecisionRuleResponseExpression(Builder<?> b)
	{
		this.id = b.id;
		this.effect = b.effect;
		this.attributeExpressions = b.attributes.build();
		this.hashCode = Objects.hashCode(id, effect, attributeExpressions);
	}

	/**
	 * Gets decision rule response
	 * unique identifier
	 *
	 * @return an unique identifier
	 */
	public String getId(){
		return id;
	}

	/**
	 * Gets {@link Effect} defaultProvider
	 *
	 * @return {@link Effect} defaultProvider
	 */
	public Effect getEffect(){
		return effect;
	}

	/**
	 * Tests if this decision info expression
	 * is applicable for a given {@link Decision}
	 *
	 * @param result a decision result
	 * @return {@code true} if an expression is applicable
	 */
	public boolean isApplicable(Decision result){
		return (result == Decision.PERMIT && effect == Effect.PERMIT) ||
		(result == Decision.DENY && effect == Effect.DENY);
	}

	public Collection<AttributeAssignmentExpression> getAttributeAssignmentExpressions(){
		return attributeExpressions.values();
	}

	/**
	 * Evaluates collection of {@link AttributeAssignmentExpression} instances
	 * and return collection of {@link AttributeAssignment} instances
	 * @param context an evaluation context
	 * @return collection of {@link AttributeAssignment} instances
	 * @throws EvaluationException if an evaluation error occurs
	 */
	protected Collection<AttributeAssignment> evaluateAttributeAssignments(
			EvaluationContext context)
		throws EvaluationException
	{

		try {
			ImmutableList.Builder<AttributeAssignment> attr = ImmutableList.builder();
			for (AttributeAssignmentExpression attrExp : attributeExpressions.values()) {
				AttributeAssignment.Builder b = AttributeAssignment.builder(attrExp.getAttributeId())
				                                                   .category(attrExp.getCategory())
				                                                   .issuer(attrExp.getIssuer());
				ValueExp val = attrExp.evaluate(context);
				if (val instanceof Value) {
					attr.add(b.value((Value) val).build());
					continue;
				}
				BagOfValues bag = (BagOfValues) val;
				for (Value v : bag.values()) {
					attr.add(b.value(v).build());
				}
			}
			return attr.build();
		}catch (EvaluationException e){
			throw e;
		}catch (Exception e){
			throw new EvaluationException(Status.processingError()
			                                    .error(e)
			                                    .build(), e);
		}
	}

	@Override
	public int hashCode(){
		return hashCode;
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
		.add("id", id)
		.add("effect", effect)
		.add("expressions", attributeExpressions)
		.toString();
	}

	public static abstract class Builder<T extends Builder<?>>
	{
		private String id;
		private Effect effect;
		private ImmutableListMultimap.Builder<String, AttributeAssignmentExpression> attributes = ImmutableListMultimap.builder();

		public T effect(Effect effect){
			Preconditions.checkNotNull(effect);
			this.effect = effect;
			return getThis();
		}

		public T id(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
			this.id = id;
			return getThis();
		}

		public T attribute(String id, Expression exp){
			return attribute(AttributeAssignmentExpression.builder(id).expression(exp));
		}

		public T attribute(String id, CategoryId category, Expression exp){
			return attribute(AttributeAssignmentExpression
					.builder(id)
					.category(category)
					.expression(exp));
		}

		public T attribute(String id, CategoryId category, String issuer, Expression exp){
			return attribute(AttributeAssignmentExpression.builder(id).category(category).issuer(issuer).expression(exp));
		}

		public T attribute(AttributeAssignmentExpression ...attrs){
			for(AttributeAssignmentExpression a : attrs){
				attributes.put(a.getAttributeId(), a);
			}
			return getThis();
		}

		public T attribute(Iterable<AttributeAssignmentExpression> attrs){
			for(AttributeAssignmentExpression a : attrs){
				attributes.put(a.getAttributeId(), a);
			}
			return getThis();
		}

		public T attribute(
				AttributeAssignmentExpression.Builder b)
		{
			AttributeAssignmentExpression exp = b.build();
			attributes.put(exp.getAttributeId(), exp);
			return getThis();
		}

		protected abstract T getThis();
	}
}
