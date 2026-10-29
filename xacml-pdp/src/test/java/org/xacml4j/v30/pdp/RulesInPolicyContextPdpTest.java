package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.VariableDefinition;
import org.xacml4j.v30.policy.VariableReference;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * A rule whose evaluation can run code of another implementation keeps a context of its own, whose
 * parent is the context of its policy, also where the policy evaluates its other rules in the policy's
 * context; a policy reached through a reference decides as when it is observed (change
 * optimize-pdp-evaluation, design D47, plan review PR-D47-argon). Every test compares a decision with the
 * same decision observed, whose rules keep contexts of their own, with the target index off and on.
 */
class RulesInPolicyContextPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final AttributeDesignatorKey PRESENT = key("present");
	private static final AttributeDesignatorKey ABSENT = key("absent");
	private static final RequestContext REQUEST = RequestContext.builder()
			.attributes(Category.subjectAccess().entity(Entity.builder()
					.attribute(Attribute.builder(PRESENT.getAttributeId()).value(StringVal.of("yes")).build()).build()).build())
			.build();
	/** The policy named by the parent of the context of a call: an engine rule context's parent is its policy's context */
	private static final FunctionSpec PARENT_POLICY = FunctionSpecBuilder.builder("urn:example:function:parent-policy")
			.param(XacmlTypes.STRING)
			.build(XacmlTypes.STRING, invocation((context, arguments) -> StringVal.of(parentPolicy(context))));
	/** Whether the parent of the context of a call names the policy of the first argument */
	private static final FunctionSpec PARENT_POLICY_IS = FunctionSpecBuilder.builder("urn:example:function:parent-policy-is")
			.param(XacmlTypes.STRING).param(XacmlTypes.STRING)
			.build(XacmlTypes.BOOLEAN, invocation((context, arguments) ->
					BooleanVal.of(parentPolicy(context).equals(((StringVal)arguments.get(0)).get()))));

	static Stream<Arguments> placements(){
		return Stream.of("condition", "function reference", "target", "obligation", "literal bag")
				.flatMap(placement -> Stream.of(false, true).map(indexed -> Arguments.of(placement, indexed)));
	}

	/**
	 * The function is called in a condition, through a reference that a built-in higher-order function
	 * calls, as the predicate of a target match, and in an obligation; or a value of another
	 * implementation in a literal bag is evaluated by a built-in higher-order function. A rule of the
	 * engine's functions that cannot apply comes first.
	 */
	@ParameterizedTest(name = "{0}, indexed={1}")
	@MethodSource("placements")
	void codeOfAnotherImplementationSeesTheContextOfItsRule(String placement, boolean indexed){
		Rule.Builder calling = Rule.builder("calling", Effect.PERMIT);
		switch(placement){
			case "condition" -> calling.condition(new Condition(apply("string-equal",
					Apply.builder(PARENT_POLICY).param(StringVal.of("policy")).build(), StringVal.of("policy"))));
			case "function reference" -> calling.condition(new Condition(anyOf(PARENT_POLICY_IS, designator(PRESENT, false))));
			case "target" -> calling.target(Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
					.match(StringVal.of("policy"), designator(PRESENT, false), PARENT_POLICY_IS).build()).build()));
			case "obligation" -> calling.obligation(ObligationExpression.builder("parent", Effect.PERMIT)
					.attribute("policy", Apply.builder(PARENT_POLICY).param(StringVal.of("policy")).build()));
			default -> calling.condition(new Condition(anyOf(FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow(),
					XacmlTypes.STRING.bagOf(new ParentPolicyValue()))));
		}
		Policy policy = Policy.builder("policy").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("first", Effect.DENY).condition(new Condition(apply("string-equal",
						apply("string-one-and-only", designator(PRESENT, true)), StringVal.of("no")))))
				.rule(calling)
				.build();
		PolicyRepository repository = PolicyRepository.build("rules-in-policy-context").build();

		Result result = decide(policy, repository, indexed, false);

		assertEquals(Decision.PERMIT, result.getDecision());
		if(placement.equals("obligation")){
			assertEquals(List.of(StringVal.of("policy")), result.getObligations().stream()
					.flatMap(o -> o.getAttributes().stream()).map(AttributeAssignment::getAttribute).toList());
		}
		assertEquals(decide(policy, repository, indexed, true), result);
	}

	/** Two rules fail, the second without a status of its own, and a third denies */
	@ParameterizedTest(name = "indexed={0}")
	@ValueSource(booleans = {false, true})
	void aPolicyReachedThroughAReferenceDecidesAsWhenObserved(boolean indexed){
		Policy referenced = Policy.builder("referenced").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "3.0:rule-combining-algorithm:permit-overrides"))
				.rule(Rule.builder("missing", Effect.PERMIT).condition(new Condition(apply("string-equal",
						apply("string-one-and-only", designator(ABSENT, true)), StringVal.of("yes")))))
				.rule(Rule.builder("undefined", Effect.PERMIT).condition(new Condition(
						new VariableReference(new VariableDefinition("undefined", BooleanVal.of(true))))))
				.rule(Rule.builder("deny", Effect.DENY).obligation(ObligationExpression.builder("deny", Effect.DENY)
						.attribute("origin", StringVal.of("deny"))))
				.build();
		PolicySet root = PolicySet.builder("root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:first-applicable"))
				.compositeDecisionRules(PolicyIDReference.builder("referenced").versionAsString("1.+").build())
				.build();
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("rules-in-policy-context", FUNCTIONS, ALGORITHMS);
		repository.add(referenced);

		Result result = decide(root, repository, indexed, false);

		assertEquals(Decision.INDETERMINATE_DP, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
		assertEquals(decide(root, repository, indexed, true), result);
	}

	/** The id of the policy that the parent of a context names, if it names no rule; "none" otherwise */
	private static String parentPolicy(EvaluationContext context){
		EvaluationContext parent = context.getParentContext();
		CompositeDecisionRule policy = parent == null || parent.getCurrentRule() != null ? null : parent.getCurrentPolicy();
		return policy == null ? "none" : policy.getId();
	}

	/** The invocation of a function of another implementation, which gets the context of each call */
	private static FunctionInvocation invocation(BiFunction<EvaluationContext, List<Expression>, ValueExp> body){
		return new FunctionInvocation(){
			@Override
			public ValueExp invoke(FunctionSpec spec, EvaluationContext context, Expression... arguments){
				return invoke(spec, context, Arrays.asList(arguments));
			}

			@Override
			public ValueExp invoke(FunctionSpec spec, EvaluationContext context, List<Expression> arguments){
				return body.apply(context, arguments);
			}
		};
	}

	private static Result decide(CompositeDecisionRule root, PolicyRepository repository, boolean indexed, boolean observed){
		PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("rules-in-policy-context")
				.policyRepository(repository).pip(PolicyInformationPoint.builder("rules-in-policy-context").build())
				.rootPolicy(root);
		if(observed) builder.observer((pdp, request) -> "request");
		if(indexed) builder.targetIndex();
		return builder.build().decide(REQUEST).getResults().iterator().next();
	}

	/** any-of(function, "policy", bag): whether the function is true of "policy" and a value of the bag */
	private static Apply anyOf(FunctionSpec function, Expression bag){
		return Apply.builder(FUNCTIONS.getFunction(URN + "3.0:function:any-of").orElseThrow())
				.param(new FunctionReference(function), StringVal.of("policy"), bag).build();
	}

	private static Apply apply(String function, Expression... arguments){
		return Apply.builder(FUNCTIONS.getFunction(URN + "1.0:function:" + function).orElseThrow()).param(arguments).build();
	}

	/** A string of another implementation whose value, once evaluated, is the policy that the parent of the context names */
	private static final class ParentPolicyValue extends Value.OtherValue<String>
	{
		ParentPolicyValue(){
			super(XacmlTypes.STRING, "parent-policy");
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Expression> T evaluate(EvaluationContext context){
			return (T)StringVal.of(parentPolicy(context));
		}
	}

	private static AttributeDesignator designator(AttributeDesignatorKey key, boolean mustBePresent){
		return AttributeDesignator.builder().key(key).mustBePresent(mustBePresent).build();
	}

	private static AttributeDesignatorKey key(String id){
		return AttributeDesignatorKey.builder("urn:example:" + id).category(CategoryId.SUBJECT_ACCESS)
				.dataType(XacmlTypes.STRING).build();
	}
}
