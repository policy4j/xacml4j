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

import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.policy.AttributeSelector;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
public class MatchTest
{
	private FunctionSpec spec;
	private AttributeDesignator ref;
	private EvaluationContext context;
	private FunctionSpecBuilder builder;
	private FunctionInvocation invocation;
	private Rule rule;
	@BeforeEach
	public void init()
	{
		this.ref = AttributeDesignator.builder().key(AttributeDesignatorKey.builder("testId").category(CategoryId.RESOURCE).dataType(XacmlTypes.INTEGER).build()).mustBePresent(false).build();
		this.invocation = mock(FunctionInvocation.class);
		this.spec = FunctionSpecBuilder.builder("testFunctionId1")
		                               .param(XacmlTypes.INTEGER)
		                               .param(XacmlTypes.INTEGER)
		                               .build(XacmlTypes.BOOLEAN, invocation);
		this.context = mock(EvaluationContext.class);
		this.rule = Rule.builder("testRule", org.xacml4j.v30.Effect.DENY).build();
	}

	@Test
	public void testMatchEvaluation() throws EvaluationException
	{
		assertEquals(XacmlTypes.INTEGER, ref.getDataType());
		when(context.resolve(ref.getReferenceKey())).thenReturn(java.util.Optional.of(XacmlTypes.INTEGER.bagBuilder().attribute(XacmlTypes.INTEGER.ofAny(2), XacmlTypes.INTEGER.ofAny(1)).build()));
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(false);
		when(invocation.invoke(spec, context,
		                         ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).build()))
				.thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(false);
		when(invocation.invoke(spec, context,
		                         ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(1)).build()))
				.thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		InOrder order = inOrder(context, invocation);
		Match m = Match
				.builder()
				.predicate(spec)
				.attribute(XacmlTypes.INTEGER.ofAny(1))
				.attrRef(ref)
				.build();
		assertEquals(MatchResult.MATCH, m.match(context));
		order.verify(context).resolve(ref.getReferenceKey());
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context,
		                         ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).build());
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context,
		                         ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(1)).build());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).resolve(ref.getReferenceKey());
		verify(context, times(2)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context,
		                         ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).build());
		verify(invocation, times(1)).invoke(spec, context,
		                         ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(1)).build());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, invocation);
	}

	@Test
	public void testMatchEvaluationFailedToResolveAttributeException() throws EvaluationException
	{
		AttributeDesignatorKey key  = AttributeDesignatorKey.builder()
				.category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.INTEGER)
				.attributeId("testId")
				.build();
		ref = AttributeDesignator.builder().key(key).mustBePresent(true).build();
		assertEquals(XacmlTypes.INTEGER, ref.getDataType());
		when(context.resolve(ref.getReferenceKey())).thenThrow(new AttributeReferenceEvaluationException(key, "Failed"));
		ArgumentCaptor<Status> statusCapture = ArgumentCaptor.forClass(Status.class);
		ArgumentCaptor<Rule> ruleCapture = ArgumentCaptor.forClass(Rule.class);
		when(context.getCurrentRule()).thenReturn(rule);
		doNothing().when(context).setEvaluationStatus(ruleCapture.capture(), statusCapture.capture());
		InOrder order = inOrder(context, invocation);
		Match m = Match
				.builder()
				.predicate(spec)
				.attribute(XacmlTypes.INTEGER.ofAny(1))
				.attrRef(ref)
				.build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		assertEquals(StatusCode.missingAttributeError(), statusCapture.getValue().getStatusCode());
		assertEquals("Failed", statusCapture.getValue().getMessage().orElse(null));
		order.verify(context).resolve(ref.getReferenceKey());
		order.verify(context).getCurrentRule();
		order.verify(context).setEvaluationStatus(ruleCapture.capture(), statusCapture.capture());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).resolve(ref.getReferenceKey());
		verify(context, times(1)).getCurrentRule();
		verify(context, times(1)).setEvaluationStatus(ruleCapture.capture(), statusCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, invocation);
		assertSame(rule, ruleCapture.getValue());
	}

	@Test
	public void testMatchPreservesContextSelectorSyntaxError() throws EvaluationException
	{
		AttributeSelector selector = AttributeSelector.builder()
				.key(AttributeSelectorKey.builder().category(CategoryId.RESOURCE)
						.contextSelectorId("context-selector").xpath("/record/missing")
						.dataType(XacmlTypes.INTEGER).build())
				.mustBePresent(false).build();
		when(context.resolve(selector.getReferenceKey())).thenThrow(
				PathEvaluationException.invalidXpath("/record/missing", Status.syntaxError().build(), null));
		when(context.getCurrentRule()).thenReturn(rule);
		ArgumentCaptor<Status> statusCapture = ArgumentCaptor.forClass(Status.class);

		Match match = Match.builder().predicate(spec).attribute(XacmlTypes.INTEGER.ofAny(1))
				.attrRef(selector).build();
		assertEquals(MatchResult.INDETERMINATE, match.match(context));
		verify(context).setEvaluationStatus(org.mockito.ArgumentMatchers.eq(rule), statusCapture.capture());
		assertEquals(StatusCode.syntaxError(), statusCapture.getValue().getStatusCode());
	}

	@Test
	public void testXPathExtensionFunctionFailureMakesMatchIndeterminate() throws EvaluationException
	{
		AttributeSelectorKey key = AttributeSelectorKey.builder()
				.category(CategoryId.RESOURCE)
				.xpath("/*[java:java.lang.Math.abs(-1) = 1]/value/text()")
				.dataType(XacmlTypes.INTEGER)
				.build();
		AttributeSelector selector = AttributeSelector.builder().key(key).mustBePresent(true).build();
		XmlContent content = XmlContent.of(
				"<record xmlns:java=\"http://xml.apache.org/xalan/java\"><value>1</value></record>");
		when(context.resolve(key)).thenAnswer(call -> content.resolve(key));
		when(context.getCurrentRule()).thenReturn(rule);

		Match match = Match.builder().predicate(spec).attribute(XacmlTypes.INTEGER.ofAny(1))
				.attrRef(selector).build();
		assertEquals(MatchResult.INDETERMINATE, match.match(context));
		verify(context).resolve(key);
		verify(context).getCurrentRule();
		verify(context).setEvaluationStatus(eq(rule), any(Status.class));
		verifyNoMoreInteractions(context, invocation);
	}

	/**
	 * Characterization for cycle C of change optimize-pdp-evaluation (review rv005): the bag
	 * [2, 3, 2] iterates as [2, 2, 3] (equal values grouped), so a predicate that is false for the
	 * first 2 and fails on the second 2 gives Indeterminate before 3 is tested. An
	 * insertion-order bag would test 3 second and give Match. Today's result must stay; changing
	 * it is the semantics question of bug policy4j-0972 (XACML 3.0 section 7.6).
	 */
	@Test
	public void failureOnAGroupedDuplicateComesBeforeALaterMatchingValue() throws EvaluationException
	{
		java.util.List<Expression> withTwo = ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(2)).build();
		java.util.List<Expression> withThree = ImmutableList.<Expression>builder().add(XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(3)).build();
		when(context.resolve(ref.getReferenceKey())).thenReturn(java.util.Optional.of(XacmlTypes.INTEGER.bagBuilder()
				.attribute(XacmlTypes.INTEGER.ofAny(2), XacmlTypes.INTEGER.ofAny(3), XacmlTypes.INTEGER.ofAny(2)).build()));
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(false);
		when(context.getCurrentRule()).thenReturn(rule);
		when(invocation.invoke(spec, context, withTwo))
				.thenReturn(XacmlTypes.BOOLEAN.ofAny(false))
				.thenThrow(new AttributeReferenceEvaluationException(ref.getReferenceKey(), "second value fails"));
		lenient().when(invocation.invoke(spec, context, withThree)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		Match m = Match.builder().predicate(spec).attribute(XacmlTypes.INTEGER.ofAny(1)).attrRef(ref).build();

		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		verify(invocation, times(2)).invoke(spec, context, withTwo);
		verify(invocation, never()).invoke(spec, context, withThree);
	}
}
