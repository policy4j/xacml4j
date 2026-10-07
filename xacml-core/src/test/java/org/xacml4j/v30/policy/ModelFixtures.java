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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

/** Real model fixtures; only expression and resolver interfaces are mocked. */
public final class ModelFixtures
{
	private static final FunctionSpec BOOLEAN_EQUAL = FunctionProviderBuilder.builder()
			.build()
			.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-equal").orElseThrow();

	private ModelFixtures(){}

	static Expression reference()
	{
		return mock(Expression.class);
	}

	static Match match(Expression expression)
	{
		AttributeReference reference = new ExpressionReference(expression);
		Match match = Match.builder().attribute(BooleanVal.of(true)).attrRef(reference).predicate(BOOLEAN_EQUAL).build();
		return match;
	}

	static MatchAllOf allOf(Expression reference)
	{
		return MatchAllOf.builder().allOf(match(reference)).build();
	}

	static MatchAnyOf anyOf(Expression reference)
	{
		return MatchAnyOf.builder().anyOf(allOf(reference)).build();
	}

	static Target target(Expression reference)
	{
		return Target.builder().anyOf(anyOf(reference)).build();
	}

	static Condition condition(Expression expression)
	{
		when(expression.getEvaluatesTo()).thenReturn(XacmlTypes.BOOLEAN);
		Condition condition = new Condition(expression);
		verify(expression).getEvaluatesTo();
		return condition;
	}

	static BagOfValues matchValues(MatchResult result)
	{
		if(result == MatchResult.INDETERMINATE){
			throw new IllegalArgumentException("Indeterminate requires an evaluation error");
		}
		return result == MatchResult.MATCH ? XacmlTypes.BOOLEAN.bagOf(BooleanVal.of(true)) : XacmlTypes.BOOLEAN.emptyBag();
	}

	static EvaluationException evaluationError()
	{
		return new EvaluationException(Status.processingError().build(), "Fixture evaluation failed");
	}

	static EvaluationContext context()
	{
		return context(false);
	}

	public static EvaluationContext context(boolean validate)
	{
		PolicyReferenceResolver resolver = mock(PolicyReferenceResolver.class, invocation -> {
			throw new AssertionError("Unexpected policy resolution: " + invocation.getMethod());
		});
		EvaluationContextHandler handler = mock(EvaluationContextHandler.class, invocation -> {
			throw new AssertionError("Unexpected attribute resolution: " + invocation.getMethod());
		});
		return new RootEvaluationContext(validate, Duration.ZERO, resolver, handler);
	}

	/** A concrete reference lets composition tests control resolution through an expression interface. */
	private static final class ExpressionReference extends AttributeReference
	{
		private static final AttributeDesignatorKey KEY = AttributeDesignatorKey.builder("test-boolean-reference")
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.BOOLEAN).build();
		private final Expression expression;

		private ExpressionReference(Expression expression)
		{
			super(AttributeDesignator.builder().key(KEY).mustBePresent(true));
			this.expression = expression;
		}

		@Override
		public AttributeDesignatorKey getReferenceKey()
		{
			return KEY;
		}

		@Override
		public void accept(ExpressionVisitor visitor)
		{
			throw new AssertionError("Unexpected visit of an evaluation fixture");
		}

		@Override
		public BagOfValues evaluate(EvaluationContext context)
		{
			return (BagOfValues)expression.evaluate(context);
		}

		@Override
		protected Optional<BagOfValues> doContextResolve(EvaluationContext context)
		{
			return Optional.of(evaluate(context));
		}
	}
}
