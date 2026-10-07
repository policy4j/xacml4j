package org.xacml4j.v30.pdp;

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
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.AttributeSelector;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

class AttributeResolutionSafetyNetTest
{
	private static final String ATTRIBUTE_ID = "urn:test:attribute";
	private static final String VALUE = "match";
	private static final String STRING_IS_IN = "urn:oasis:names:tc:xacml:1.0:function:string-is-in";
	private static final String ATTRIBUTE_DESIGNATOR = "urn:oasis:names:tc:xacml:3.0:function:attribute-designator";
	private static final String BOOLEAN_ONE_AND_ONLY = "urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only";

	static Stream<Arguments> designatorResolutionMatrix()
	{
		return Stream.of(
				Arguments.of("present-must-be-present-false", false, false, Decision.PERMIT, StatusCodeId.OK),
				Arguments.of("present-must-be-present-true", true, false, Decision.PERMIT, StatusCodeId.OK),
				Arguments.of("missing-must-be-present-false", false, false, Decision.NOT_APPLICABLE, StatusCodeId.OK),
				Arguments.of("missing-must-be-present-true", true, false, Decision.INDETERMINATE_P, StatusCodeId.MISSING_ATTRIBUTE),
				// Maintainer ruling 2026-10-07 (run 061 D3): a resolver failure fails closed.
				Arguments.of("resolver-failure-must-be-present-false", false, true, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR),
				Arguments.of("resolver-failure-must-be-present-true", true, true, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("designatorResolutionMatrix")
	void designatorResultIsBagAndPdpReportsDecisionAndStatus(
			String name, boolean mustBePresent, boolean resolverFailure,
			Decision expectedDecision, StatusCodeId expectedStatus)
	{
		AttributeDesignator designator = designator(mustBePresent, null);
		assertThat(designator.getEvaluatesTo()).isEqualTo(XacmlTypes.STRING.bagType());

		RequestContext request = name.startsWith("present")
				? requestWithString("issuer-a", VALUE)
				: RequestContext.builder().build();
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		stubResolver(pip, resolverFailure);
		Result result = pdp(stringIsIn(designator), Effect.PERMIT, pip).decide(request)
				.getResults().stream().findFirst().orElseThrow();
		assertDecisionAndStatus(result, expectedDecision, expectedStatus);
		if (name.startsWith("present")) {
			verifyNoMoreInteractions(pip);
		} else {
			verify(pip, times(1)).resolve(any(), any(AttributeDesignatorKey.class));
			verifyNoMoreInteractions(pip);
		}
	}

	@Test
	void pipCanSupplyAnAttributeMissingFromTheRequest()
	{
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(), any(AttributeDesignatorKey.class)))
				.thenReturn(Optional.of(XacmlTypes.STRING.ofAny(VALUE).toBag()));
		Result result = pdp(stringIsIn(designator(false, null)), Effect.PERMIT, pip)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(result, Decision.PERMIT, StatusCodeId.OK);
		verify(pip, times(1)).resolve(any(), any(AttributeDesignatorKey.class));
		verifyNoMoreInteractions(pip);
	}

	@Test
	void pipCanSupplyAnAttributeSelectorValue()
	{
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		AttributeSelectorKey selectorKey = AttributeSelectorKey.builder()
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).xpath("/missing").build();
		when(pip.resolve(any(), any(AttributeSelectorKey.class)))
				.thenReturn(Optional.of(XacmlTypes.STRING.ofAny(VALUE).toBag()));
		AttributeSelector selector = AttributeSelector.builder().key(selectorKey).build();

		Result result = pdp(stringIsIn(selector), Effect.PERMIT, pip)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(result, Decision.PERMIT, StatusCodeId.OK);
		verify(pip, times(1)).resolve(any(), any(AttributeSelectorKey.class));
		verifyNoMoreInteractions(pip);
	}

	@Test
	void selectorResolverFailureCallsTheSelectorOverload()
	{
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(), any(AttributeSelectorKey.class)))
				.thenThrow(new IllegalStateException("selector resolver failed"));
		AttributeSelector selector = AttributeSelector.builder()
				.key(AttributeSelectorKey.builder().category(CategoryId.RESOURCE)
						.dataType(XacmlTypes.STRING).xpath("/missing").build())
				.mustBePresent(true).build();

		Result result = pdp(stringIsIn(selector), Effect.PERMIT, pip)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(result, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
		// The response message is the fixed text, not "selector resolver failed".
		assertThat(result.getStatus().getMessage().orElse(null))
				.isEqualTo(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE);
		verify(pip, times(1)).resolve(any(), any(AttributeSelectorKey.class));
		verifyNoMoreInteractions(pip);
	}

	@Test
	void selectorResolverFailureIsProcessingErrorWhenNotMustBePresent()
	{
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(), any(AttributeSelectorKey.class)))
				.thenThrow(new IllegalStateException("selector resolver failed"));
		AttributeSelector selector = AttributeSelector.builder()
				.key(AttributeSelectorKey.builder().category(CategoryId.RESOURCE)
						.dataType(XacmlTypes.STRING).xpath("/missing").build())
				.mustBePresent(false).build();

		Result result = pdp(stringIsIn(selector), Effect.PERMIT, pip)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(result, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
		// The response message is the fixed text, not "selector resolver failed".
		assertThat(result.getStatus().getMessage().orElse(null))
				.isEqualTo(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE);
		verify(pip, times(1)).resolve(any(), any(AttributeSelectorKey.class));
		verifyNoMoreInteractions(pip);
	}

	@Test
	void denyRuleStaysIndeterminateWhenItsResolverFails()
	{
		// The fail-open risk of bead 4.34: a Deny rule must not stop applying when its PIP fails.
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		stubResolver(pip, true);

		Result result = pdp(stringIsIn(designator(false, null)), Effect.DENY, pip)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(result, Decision.INDETERMINATE_D, StatusCodeId.STATUS_PROCESSING_ERROR);
		verify(pip, times(1)).resolve(any(), any(AttributeDesignatorKey.class));
		verifyNoMoreInteractions(pip);
	}

	@Test
	void resolverFailureMustBeIndeterminateWithProcessingErrorWhenNotMustBePresent()
	{
		assertResolverFailureIsProcessingError(false);
	}

	@Test
	void resolverFailureMustBeIndeterminateWithProcessingErrorWhenMustBePresent()
	{
		assertResolverFailureIsProcessingError(true);
	}

	private static void assertResolverFailureIsProcessingError(boolean mustBePresent)
	{
		Result result = pdp(stringIsIn(designator(mustBePresent, null)), true)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
		assertThat(result.getDecision()).isEqualTo(Decision.INDETERMINATE_P);
		assertThat(result.getStatus().getStatusCode().getValue())
				.isEqualTo(org.xacml4j.v30.StatusCodeId.STATUS_PROCESSING_ERROR);
		// The status message goes into the response: it is a fixed text, not the resolver's message.
		assertThat(result.getStatus().getMessage().orElse(null))
				.isEqualTo(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE);
	}

	@ParameterizedTest(name = "mustBePresent={0}")
	@ValueSource(booleans = {false, true})
	void designatorResolverCannotChooseTheStatusOrTheResponseMessage(boolean mustBePresent)
	{
		// A resolver's own syntax-error is still a retrieval failure (run 061 D3), and its
		// message can hold back-end details.
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(), any(AttributeDesignatorKey.class)))
				.thenThrow(new EvaluationException(
						Status.syntaxError().message("backend query: secret").build(),
						"backend query: secret"));

		Result result = pdp(stringIsIn(designator(mustBePresent, null)), Effect.PERMIT, pip)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(result, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
		assertThat(result.getStatus().getMessage().orElse("")).doesNotContain("secret");
		verify(pip, times(1)).resolve(any(), any(AttributeDesignatorKey.class));
		verifyNoMoreInteractions(pip);
	}

	@ParameterizedTest(name = "mustBePresent={0}")
	@ValueSource(booleans = {false, true})
	void selectorResolverCannotChooseTheStatusOrTheResponseMessage(boolean mustBePresent)
	{
		// rv079 and rv080 round 3: a path exception with status syntax-error from the information
		// point is a retrieval failure too. Only the engine's own path evaluation reports syntax-error.
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(), any(AttributeSelectorKey.class)))
				.thenThrow(org.xacml4j.v30.PathEvaluationException.invalidXpath("/missing",
						Status.syntaxError().message("backend query: secret").build(), null));
		AttributeSelector selector = AttributeSelector.builder()
				.key(AttributeSelectorKey.builder().category(CategoryId.RESOURCE)
						.dataType(XacmlTypes.STRING).xpath("/missing").build())
				.mustBePresent(mustBePresent).build();

		Result result = pdp(stringIsIn(selector), Effect.PERMIT, pip)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(result, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
		assertThat(result.getStatus().getMessage().orElse(null))
				.isEqualTo(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE);
		verify(pip, times(1)).resolve(any(), any(AttributeSelectorKey.class));
		verifyNoMoreInteractions(pip);
	}

	@Test
	void attributeDesignatorFunctionWithAbsentCategoryYieldsEmptyBag()
	{
		Expression function = Apply.builder(ATTRIBUTE_DESIGNATOR, defaultFunctions()::getFunction)
				.param(XacmlTypes.ANYURI.ofAny(CategoryId.RESOURCE.getId()),
						XacmlTypes.ANYURI.ofAny(ATTRIBUTE_ID),
						XacmlTypes.ANYURI.ofAny(XacmlTypes.STRING.getTypeId()), BooleanVal.of(false))
				.build();
		Result result = pdp(stringIsIn(function), false).decide(RequestContext.builder().build())
				.getResults().stream().findFirst().orElseThrow();
		assertDecisionAndStatus(result, Decision.NOT_APPLICABLE, StatusCodeId.OK);
	}

	@Test
	void attributeDesignatorFunctionWithAbsentCategoryAndMustBePresentYieldsMissingAttribute()
	{
		Expression function = attributeDesignatorFunction(
				XacmlTypes.ANYURI.ofAny(CategoryId.RESOURCE.getId()), true, null);
		Result result = pdp(stringIsIn(function), false).decide(RequestContext.builder().build())
				.getResults().stream().findFirst().orElseThrow();
		assertDecisionAndStatus(result, Decision.INDETERMINATE_P, StatusCodeId.MISSING_ATTRIBUTE);
	}

	@Test
	void attributeDesignatorFunctionResolvesByCategoryUriAndEntity()
	{
		Expression categoryFunction = attributeDesignatorFunction(
				XacmlTypes.ANYURI.ofAny(CategoryId.RESOURCE.getId()), false, null);
		Entity entity = Entity.builder().attribute(Attribute.builder(ATTRIBUTE_ID)
				.issuer("issuer-a").value(StringVal.of(VALUE)).build()).build();
		Expression entityFunction = attributeDesignatorFunction(entity, false, null);
		RequestContext request = requestWithString("issuer-a", VALUE);
		assertDecisionAndStatus(pdp(stringIsIn(categoryFunction), false).decide(request).getResults().stream()
				.findFirst().orElseThrow(), Decision.PERMIT, StatusCodeId.OK);
		assertDecisionAndStatus(pdp(stringIsIn(entityFunction), false).decide(RequestContext.builder().build())
				.getResults().stream().findFirst().orElseThrow(), Decision.PERMIT, StatusCodeId.OK);
	}

	@Test
	void attributeDesignatorFunctionOptionalMissingAttributeIsNotApplicableForCategoryAndEntity()
	{
		Expression optionalCategory = attributeDesignatorFunction(
				XacmlTypes.ANYURI.ofAny(CategoryId.RESOURCE.getId()), false, null);
		Entity emptyEntity = Entity.builder().build();
		Expression optionalEntity = attributeDesignatorFunction(emptyEntity, false, null);
		Entity resourceWithOtherAttribute = Entity.builder()
				.attribute(Attribute.builder("urn:test:other").value(StringVal.of("other")).build()).build();
		RequestContext emptyResource = RequestContext.builder()
				.attributes(Category.builder(CategoryId.RESOURCE).entity(resourceWithOtherAttribute).build()).build();

		assertDecisionAndStatus(pdp(stringIsIn(optionalCategory), false).decide(emptyResource)
				.getResults().stream().findFirst().orElseThrow(), Decision.NOT_APPLICABLE, StatusCodeId.OK);
		assertDecisionAndStatus(pdp(stringIsIn(optionalEntity), false).decide(RequestContext.builder().build())
				.getResults().stream().findFirst().orElseThrow(), Decision.NOT_APPLICABLE, StatusCodeId.OK);
	}

	@Test
	void attributeDesignatorFunctionRequiredMissingAttributeIsIndeterminateForCategoryAndEntity()
	{
		Expression requiredCategory = attributeDesignatorFunction(
				XacmlTypes.ANYURI.ofAny(CategoryId.RESOURCE.getId()), true, null);
		Entity emptyEntity = Entity.builder().build();
		Expression requiredEntity = attributeDesignatorFunction(emptyEntity, true, null);
		Entity resourceWithOtherAttribute = Entity.builder()
				.attribute(Attribute.builder("urn:test:other").value(StringVal.of("other")).build()).build();
		RequestContext emptyResource = RequestContext.builder()
				.attributes(Category.builder(CategoryId.RESOURCE).entity(resourceWithOtherAttribute).build()).build();

		assertDecisionAndStatus(pdp(stringIsIn(requiredCategory), false).decide(emptyResource)
				.getResults().stream().findFirst().orElseThrow(), Decision.INDETERMINATE_P, StatusCodeId.MISSING_ATTRIBUTE);
		assertDecisionAndStatus(pdp(stringIsIn(requiredEntity), false).decide(RequestContext.builder().build())
				.getResults().stream().findFirst().orElseThrow(), Decision.INDETERMINATE_P, StatusCodeId.MISSING_ATTRIBUTE);
	}

	private static Expression attributeDesignatorFunction(org.xacml4j.v30.types.Value<?> categoryOrEntity,
			boolean mustBePresent, String issuer)
	{
		Apply.Builder function = Apply.builder(ATTRIBUTE_DESIGNATOR, defaultFunctions()::getFunction)
				.param(categoryOrEntity, XacmlTypes.ANYURI.ofAny(ATTRIBUTE_ID),
						XacmlTypes.ANYURI.ofAny(XacmlTypes.STRING.getTypeId()), BooleanVal.of(mustBePresent));
		if (issuer != null) {
			function.param(StringVal.of(issuer));
		}
		return function.build();
	}

	@Test
	void issuerFiltersRequestValues()
	{
		AttributeDesignator matching = designator(true, "issuer-a");
		AttributeDesignator nonmatching = designator(false, "issuer-b");
		Result match = pdp(stringIsIn(matching), false)
				.decide(requestWithString("issuer-a", VALUE)).getResults().stream().findFirst().orElseThrow();
		Result mismatch = pdp(stringIsIn(nonmatching), false)
				.decide(requestWithString("issuer-a", VALUE)).getResults().stream().findFirst().orElseThrow();
		assertDecisionAndStatus(match, Decision.PERMIT, StatusCodeId.OK);
		assertDecisionAndStatus(mismatch, Decision.NOT_APPLICABLE, StatusCodeId.OK);
	}

	@Test
	void issuerFilterRejectsNullIssuerWhenMustBePresent()
	{
		Result result = pdp(stringIsIn(designator(true, "issuer-a")), false)
				.decide(requestWithString(null, VALUE)).getResults().stream().findFirst().orElseThrow();
		assertDecisionAndStatus(result, Decision.INDETERMINATE_P, StatusCodeId.MISSING_ATTRIBUTE);
	}

	@Test
	void designatorDoesNotMatchWrongCategoryOrDataType()
	{
		AttributeDesignator wrongCategory = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder(ATTRIBUTE_ID).category(CategoryId.SUBJECT_ACCESS)
						.dataType(XacmlTypes.STRING).build())
				.mustBePresent(false).build();
		AttributeDesignator wrongDataType = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder(ATTRIBUTE_ID).category(CategoryId.RESOURCE)
						.dataType(XacmlTypes.INTEGER).build())
				.mustBePresent(false).build();
		Result categoryResult = pdp(stringIsIn(wrongCategory), false)
				.decide(requestWithString("issuer-a", VALUE)).getResults().stream().findFirst().orElseThrow();
		Result typeResult = pdp(integerIsIn(wrongDataType), false)
				.decide(requestWithString("issuer-a", VALUE)).getResults().stream().findFirst().orElseThrow();
		assertDecisionAndStatus(categoryResult, Decision.NOT_APPLICABLE, StatusCodeId.OK);
		assertDecisionAndStatus(typeResult, Decision.NOT_APPLICABLE, StatusCodeId.OK);
	}

	@Test
	void ruleConditionHandlesAbsentTrueFalseEmptyAndMultipleValues()
	{
		Result absent = pdp(null, false).decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
		Result trueCondition = pdp(BooleanVal.of(true), false)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
		Result falseCondition = pdp(BooleanVal.of(false), false)
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
		AttributeDesignator boolDesignator = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder("urn:test:boolean")
						.category(CategoryId.RESOURCE).dataType(XacmlTypes.BOOLEAN).build())
				.build();
		Expression oneAndOnly = Apply.builder(BOOLEAN_ONE_AND_ONLY, defaultFunctions()::getFunction)
				.param(boolDesignator).build();
		Result emptyResult = pdp(oneAndOnly, false).decide(RequestContext.builder().build())
				.getResults().stream().findFirst().orElseThrow();
		RequestContext multipleValues = requestWithBooleans(true, false);
		Result multipleResult = pdp(oneAndOnly, false).decide(multipleValues).getResults().stream().findFirst().orElseThrow();

		assertDecisionAndStatus(absent, Decision.PERMIT, StatusCodeId.OK);
		assertDecisionAndStatus(trueCondition, Decision.PERMIT, StatusCodeId.OK);
		assertDecisionAndStatus(falseCondition, Decision.NOT_APPLICABLE, StatusCodeId.OK);
		assertDecisionAndStatus(emptyResult, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
		assertDecisionAndStatus(multipleResult, Decision.INDETERMINATE_P, StatusCodeId.STATUS_PROCESSING_ERROR);
	}

	@Test
	void denyRuleConditionHandlesAbsentTrueAndIndeterminate()
	{
		Result absent = pdp(null, Effect.DENY, mock(PolicyInformationPoint.class))
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
		Result trueCondition = pdp(BooleanVal.of(true), Effect.DENY, mock(PolicyInformationPoint.class))
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
		AttributeDesignator boolDesignator = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder("urn:test:boolean")
						.category(CategoryId.RESOURCE).dataType(XacmlTypes.BOOLEAN).build())
				.build();
		Expression oneAndOnly = Apply.builder(BOOLEAN_ONE_AND_ONLY, defaultFunctions()::getFunction)
				.param(boolDesignator).build();
		Result indeterminate = pdp(oneAndOnly, Effect.DENY, mock(PolicyInformationPoint.class))
				.decide(RequestContext.builder().build()).getResults().stream().findFirst().orElseThrow();
		assertDecisionAndStatus(absent, Decision.DENY, StatusCodeId.OK);
		assertDecisionAndStatus(trueCondition, Decision.DENY, StatusCodeId.OK);
		assertDecisionAndStatus(indeterminate, Decision.INDETERMINATE_D, StatusCodeId.STATUS_PROCESSING_ERROR);
	}

	private static void assertDecisionAndStatus(Result result, Decision decision, StatusCodeId status)
	{
		assertThat(result.getDecision()).isEqualTo(decision);
		assertThat(result.getStatus().getStatusCode().getValue()).isEqualTo(status);
	}

	private static Expression stringIsIn(Expression designator)
	{
		return Apply.builder(STRING_IS_IN, defaultFunctions()::getFunction)
				.param(StringVal.of(VALUE), designator)
				.build();
	}

	private static Expression integerIsIn(Expression designator)
	{
		return Apply.builder("urn:oasis:names:tc:xacml:1.0:function:integer-is-in", defaultFunctions()::getFunction)
				.param(XacmlTypes.INTEGER.ofAny(1), designator).build();
	}

	private static FunctionProvider defaultFunctions()
	{
		return FunctionProvider.builder().withDefaultFunctions().build();
	}

	private static AttributeDesignator designator(boolean mustBePresent, String issuer)
	{
		return AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder(ATTRIBUTE_ID)
						.category(CategoryId.RESOURCE)
						.dataType(XacmlTypes.STRING)
						.issuer(issuer)
						.build())
				.mustBePresent(mustBePresent)
				.build();
	}

	private static RequestContext requestWithString(String issuer, String value)
	{
		return RequestContext.builder()
				.attributes(Category.builder(CategoryId.RESOURCE)
						.entity(Entity.builder()
								.attribute(Attribute.builder(ATTRIBUTE_ID)
										.issuer(issuer).value(StringVal.of(value)).build())
								.build())
						.build())
				.build();
	}

	private static RequestContext requestWithBooleans(boolean... values)
	{
		Attribute.Builder attribute = Attribute.builder("urn:test:boolean");
		for (boolean value : values) {
			attribute.value(BooleanVal.of(value));
		}
		return RequestContext.builder()
				.attributes(Category.builder(CategoryId.RESOURCE)
						.entity(Entity.builder().attribute(attribute.build()).build())
						.build())
				.build();
	}

	private static void stubResolver(PolicyInformationPoint pip, boolean resolverFailure)
	{
		if (resolverFailure) {
			when(pip.resolve(any(), any(AttributeDesignatorKey.class)))
					.thenThrow(new IllegalStateException("resolver failed"));
		} else {
			when(pip.resolve(any(), any(AttributeDesignatorKey.class))).thenReturn(Optional.empty());
		}
	}

	private static PolicyDecisionPoint pdp(Expression condition, boolean resolverFailure)
	{
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		stubResolver(pip, resolverFailure);
		return pdp(condition, Effect.PERMIT, pip);
	}

	private static PolicyDecisionPoint pdp(Expression condition, Effect effect, PolicyInformationPoint pip)
	{
		DecisionCombiningAlgorithmProvider algorithms = DecisionCombiningAlgorithmProvider.builder()
				.withDefaultAlgorithms().build();
		DecisionCombiningAlgorithm<Rule> ruleAlgorithm = algorithms.getRuleAlgorithm(
				"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
		Rule.Builder ruleBuilder = Rule.builder("rule", effect);
		if (condition != null) {
			ruleBuilder.condition(new Condition(condition));
		}
		Policy policy = Policy.builder("policy").version("1.0")
				.combiningAlgorithm(ruleAlgorithm).rule(ruleBuilder).build();
		return PolicyDecisionPointBuilder.builder("safety-net")
				.policyRepository(PolicyRepository.build("safety-net-repository").build())
				.pip(pip).rootPolicy(policy).build();
	}
}
