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
import java.util.Optional;

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
	/**
	 * The assignments of an expression that has none or only literal values, built once; nothing
	 * when a value is evaluated (cycle AN)
	 */
	private final Optional<Collection<AttributeAssignment>> constantAssignments;

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
		this.constantAssignments = constantAssignments(attributeExpressions.values());
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
		if(constantAssignments.isPresent()){
			return constantAssignments.get();
		}
		try {
			ImmutableList.Builder<AttributeAssignment> attr = ImmutableList.builder();
			for (AttributeAssignmentExpression attrExp : attributeExpressions.values()) {
				assign(attr, attrExp, attrExp.evaluate(context));
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

	/**
	 * The assignments of this expression when they are the same for every decision: it has no
	 * assignment expressions, or only literal values
	 */
	protected final Optional<Collection<AttributeAssignment>> constantAssignments(){
		return constantAssignments;
	}

	private static Optional<Collection<AttributeAssignment>> constantAssignments(
			Collection<AttributeAssignmentExpression> expressions)
	{
		ImmutableList.Builder<AttributeAssignment> attr = ImmutableList.builder();
		for (AttributeAssignmentExpression attrExp : expressions) {
			if (!(attrExp.getExpression() instanceof ValueExp value)) {
				return Optional.empty();
			}
			assign(attr, attrExp, value);
		}
		return Optional.of(attr.build());
	}

	/** Adds the assignment of a value, or one assignment per value of a bag */
	private static void assign(ImmutableList.Builder<AttributeAssignment> attr,
			AttributeAssignmentExpression attrExp, ValueExp val)
	{
		AttributeAssignment.Builder b = AttributeAssignment.builder(attrExp.getAttributeId())
		                                                   .category(attrExp.getCategory())
		                                                   .issuer(attrExp.getIssuer());
		if (val instanceof Value) {
			attr.add(b.value((Value) val).build());
			return;
		}
		BagOfValues bag = (BagOfValues) val;
		for (Value v : bag) {
			attr.add(b.value(v).build());
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
