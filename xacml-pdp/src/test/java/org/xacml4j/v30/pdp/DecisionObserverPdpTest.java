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
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeAssignmentExpression;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.AttributeReference;
import org.xacml4j.v30.policy.AttributeSelector;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.ConditionResult;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.audit.NoAuditPolicyDecisionPointAuditor;
import org.xacml4j.v30.spi.observe.AttributeSource;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.spi.pdp.NoCachePolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.AttributeSet;
import org.xacml4j.v30.spi.pip.BasePolicyInformationPointCacheProvider;
import org.xacml4j.v30.spi.pip.ContentRef;
import org.xacml4j.v30.spi.pip.ContentResolvers;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.ResolverContext;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

import com.codahale.metrics.MetricRegistry;

/**
 * The public callback sequence of a {@link DecisionObserver} and its tokens, and how an observer
 * exception fails a decision (change optimize-pdp-evaluation, design D34)
 */
class DecisionObserverPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final RequestContext READ = RequestContext.builder()
			.attributes(Category.builder(CategoryId.ACTION).entity(Entity.builder()
					.attribute(Attribute.builder("action").value(XacmlTypes.STRING.ofAny("read")).build())
					.build()).build())
			.build();

	@Test
	void aNonMatchingChildKeepsItsObservationSequence(){
		AttributeDesignator action = designator("action", CategoryId.ACTION, false);
		Policy noMatch = policy("no-match", Rule.builder("deny", Effect.DENY).build())
				.target(target("write", action)).build();
		Policy match = policy("match", Rule.builder("permit", Effect.PERMIT).build())
				.target(target("read", action)).build();
		PolicySet root = set("root", "1.0:first-applicable").policy(noMatch).policy(match).build();
		Recorder recorder = new Recorder();

		assertEquals(Decision.PERMIT, decide(pdp(root, roles(), recorder), READ).getDecision());
		assertEquals(List.of("requestStarted", "request decisionStarted 1", "decision1 evaluationStarted root",
				"root targetMatched MATCH", "root childrenSelected 2", "root evaluationStarted no-match",
				"no-match attributeRequested action", "attr:action attributeResolved REQUEST value",
				"no-match targetMatched NOMATCH", "no-match evaluationFinished NOT_APPLICABLE",
				"root evaluationStarted match", "match attributeRequested action",
				"attr:action attributeResolved REQUEST_MEMO value", "match targetMatched MATCH",
				"match evaluationStarted permit", "permit targetMatched MATCH", "permit evaluationFinished PERMIT",
				"match evaluationFinished PERMIT", "root evaluationFinished PERMIT",
				"decision1 decisionFinished PERMIT", "request requestFinished"), recorder.events);
	}

	@Test
	void aForeignEvaluationContextCanProvideItsObservation(){
		Policy root = policy("foreign-policy", Rule.builder("r", Effect.PERMIT).build()).build();
		Recorder recorder = new Recorder();
		DecisionObservation observation = DecisionObservation.start(recorder, "request", READ, 1, 1);
		EvaluationContext foreign = mock(EvaluationContext.class);
		when(foreign.getDecisionObservation()).thenReturn(observation);

		assertEquals(Decision.PERMIT, root.evaluate(root.createContext(foreign)));
		assertEquals(List.of("request decisionStarted 1", "decision1 evaluationStarted foreign-policy",
				"foreign-policy targetMatched MATCH", "foreign-policy evaluationStarted r", "r targetMatched MATCH",
				"r evaluationFinished PERMIT", "foreign-policy evaluationFinished PERMIT"), recorder.events);
		assertFalse(RootEvaluationContext.observationOf(null).isEnabled());
	}

	@Test
	void indexedChildrenAreReportedForPolicySetsAndPolicies(){
		AttributeDesignator action = designator("action", CategoryId.ACTION, false);
		Policy excluded = policy("excluded", Rule.builder("excluded-rule", Effect.DENY).build())
				.target(target("write", action)).build();
		Policy selected = Policy.builder("selected").version("1.0").target(target("read", action))
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("write-rule", Effect.DENY).target(target("write", action)))
				.rule(Rule.builder("read-rule", Effect.PERMIT).target(target("read", action))).build();
		PolicySet root = set("root", "1.0:first-applicable").policy(excluded).policy(selected).build();
		List<String> selections = new ArrayList<>();
		Recorder recorder = new Recorder(){
			@Override
			public void childrenSelected(String current, EvaluationContext context, CompositeDecisionRule rule,
					List<? extends DecisionRule> children){
				selections.add(rule.getId() + ":" + children.stream().map(DecisionRule::getId).toList());
			}
		};
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("observed-index")
				.policyRepository(PolicyRepository.build("observed-index").build())
				.pip(roles()).rootPolicy(root).observer(recorder).targetIndex().build();

		assertEquals(Decision.PERMIT, decide(pdp, READ).getDecision());
		assertEquals(List.of("root:[selected]", "selected:[read-rule]"), selections);
	}

	@Test
	void theCallbacksFollowTheEvaluationAndNestInTheirTokens(){
		Policy p = Policy.builder("p").version("1.0")
				.target(target("read", designator("action", CategoryId.ACTION, false)))
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("r", Effect.PERMIT).condition(isIn("read", designator("action", CategoryId.ACTION, false))))
				.advice(AdviceExpression.builder("a", Effect.PERMIT).attribute(role()))
				.obligation(ObligationExpression.builder("o", Effect.PERMIT).attribute(role()))
				.build();
		PolicySet root = set("root", "3.0:deny-overrides").policy(p).build();
		Recorder recorder = new Recorder();

		Result result = decide(pdp(root, roles(), recorder), READ);

		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(List.of(
				"requestStarted",
				"request decisionStarted 1",
				"decision1 evaluationStarted root",
				"root targetMatched MATCH",
				"root childrenSelected 1",
				"root evaluationStarted p",
				"p attributeRequested action",
				"attr:action attributeResolved REQUEST value",
				"p targetMatched MATCH",
				"p evaluationStarted r",
				"r targetMatched MATCH",
				"r attributeRequested action",
				"attr:action attributeResolved REQUEST_MEMO value",
				"r conditionEvaluated TRUE",
				"r evaluationFinished PERMIT",
				"p attributeRequested role",
				"attr:role attributeResolved INFORMATION_POINT value",
				"p attributeRequested role",
				"attr:role attributeResolved INFORMATION_POINT_MEMO value",
				"p responsesStaged PERMIT 1/1",
				"p evaluationFinished PERMIT",
				"root evaluationFinished PERMIT",
				"decision1 decisionFinished PERMIT",
				"request requestFinished"), recorder.events);
	}

	/** A key that no resolver supplies is missing without an attempt, with the events of one (cycle AN) */
	@Test
	void aKeyWithoutAResolverKeepsTheEventsOfAnAttempt(){
		PolicySet root = set("root", "1.0:first-applicable")
				.target(target("x", designator("missing", CategoryId.RESOURCE, true)))
				.policy(policy("p", Rule.builder("r", Effect.PERMIT).build()).build()).build();
		Recorder recorder = new Recorder();

		Result result = decide(pdp(root, PolicyInformationPoint.builder("defaults").build(), recorder), READ);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of(
				"requestStarted",
				"request decisionStarted 1",
				"decision1 evaluationStarted root",
				"root attributeRequested missing",
				"attr:missing attributeResolved INFORMATION_POINT absent",
				"root targetMatched INDETERMINATE",
				"root passStarted",
				"pass:root childrenSelected 1",
				"pass:root evaluationStarted p",
				"p targetMatched MATCH",
				"p evaluationStarted r",
				"r targetMatched MATCH",
				"r evaluationFinished PERMIT",
				"p evaluationFinished PERMIT",
				"pass:root passFinished INDETERMINATE_P",
				"root evaluationFinished INDETERMINATE_P",
				"decision1 decisionFinished INDETERMINATE_P",
				"request requestFinished"), recorder.events);
	}

	@Test
	void anExtendedIndeterminatePassNestsTheChildrenInItsToken(){
		PolicySet root = set("root", "1.0:first-applicable")
				.target(target("x", designator("missing", CategoryId.RESOURCE, true)))
				.policy(policy("p", Rule.builder("r", Effect.PERMIT).build()).build()).build();
		Recorder recorder = new Recorder();

		Result result = decide(pdp(root, roles(), recorder), READ);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(List.of(
				"requestStarted",
				"request decisionStarted 1",
				"decision1 evaluationStarted root",
				"root attributeRequested missing",
				"attr:missing attributeResolved INFORMATION_POINT absent",
				"root targetMatched INDETERMINATE",
				"root passStarted",
				"pass:root childrenSelected 1",
				"pass:root evaluationStarted p",
				"p targetMatched MATCH",
				"p evaluationStarted r",
				"r targetMatched MATCH",
				"r evaluationFinished PERMIT",
				"p evaluationFinished PERMIT",
				"pass:root passFinished INDETERMINATE_P",
				"root evaluationFinished INDETERMINATE_P",
				"decision1 decisionFinished INDETERMINATE_P",
				"request requestFinished"), recorder.events);
	}

	@Test
	void aReferenceIsReportedWithThePolicyItReferencesInsideWhetherItsPositionIsPinnedOrNot(){
		PolicyRepository repository = PolicyRepository.build("observed-references").build();
		repository.add(policy("q", Rule.builder("q-rule", Effect.PERMIT).build()).build());
		PolicySet root = set("root", "3.0:deny-overrides")
				.compositeDecisionRules(PolicyIDReference.builder("q").build()).build();
		Recorder pinned = new Recorder();
		Recorder unpinned = new Recorder();

		decide(PolicyDecisionPointBuilder.builder("pinned").policyRepository(repository).pip(roles()).rootPolicy(root)
				.observer(pinned).build(), READ);
		decide(new DefaultPolicyDecisionPoint("unpinned", new MetricRegistry(),
				new PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory(root, repository,
						new NoAuditPolicyDecisionPointAuditor(), new NoCachePolicyDecisionCache(), XPathProvider.defaultProvider(),
						roles(), new RequestContextHandlerChain(List.of())), unpinned), READ);

		List<String> evaluation = List.of(
				"requestStarted",
				"request decisionStarted 1",
				"decision1 evaluationStarted root",
				"root targetMatched MATCH",
				"root evaluationStarted ref:q",
				"ref:q evaluationStarted q",
				"q targetMatched MATCH",
				"q evaluationStarted q-rule",
				"q-rule targetMatched MATCH",
				"q-rule evaluationFinished PERMIT",
				"q evaluationFinished PERMIT",
				"ref:q evaluationFinished PERMIT",
				"root evaluationFinished PERMIT",
				"decision1 decisionFinished PERMIT",
				"request requestFinished");
		assertEquals(evaluation, unpinned.events);
		// The prepared list of the pinned position is the only difference
		List<String> withPreparedChildren = new ArrayList<>(evaluation);
		withPreparedChildren.add(4, "root childrenSelected 1");
		assertEquals(withPreparedChildren, pinned.events);
	}

	@Test
	void aFailedFunctionIsReportedAndItsRuleCombinesAsIndeterminate(){
		FunctionSpec divide = function("1.0:function:integer-divide");
		Condition failing = Condition.condition(Apply.builder(function("1.0:function:integer-equal"))
				.param(Apply.builder(divide).param(IntegerVal.of(1), IntegerVal.of(0)).build(), IntegerVal.of(1)).build());
		Policy p = Policy.builder("p").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "3.0:rule-combining-algorithm:permit-overrides"))
				.rule(Rule.builder("bad", Effect.PERMIT).condition(failing))
				.rule(Rule.builder("good", Effect.PERMIT))
				.build();
		Recorder recorder = new Recorder();

		Result result = decide(pdp(p, roles(), recorder), READ);

		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(List.of(
				"requestStarted",
				"request decisionStarted 1",
				"decision1 evaluationStarted p",
				"p targetMatched MATCH",
				"p evaluationStarted bad",
				"bad targetMatched MATCH",
				"bad functionFailed " + divide.getShortId(),
				"bad conditionEvaluated INDETERMINATE",
				"bad evaluationFinished INDETERMINATE_P",
				"p evaluationStarted good",
				"good targetMatched MATCH",
				"good evaluationFinished PERMIT",
				"p evaluationFinished PERMIT",
				"decision1 decisionFinished PERMIT",
				"request requestFinished"), recorder.events);
	}

	@Test
	void aCachedDecisionIsReportedWithoutEvaluation(){
		PolicyDecisionCache cache = mock(PolicyDecisionCache.class);
		when(cache.getDecision(any())).thenReturn(Result.ok(Decision.DENY).build());
		Recorder recorder = new Recorder();

		Result result = decide(PolicyDecisionPointBuilder.builder("cached").policyRepository(PolicyRepository.build("cached").build())
				.pip(roles()).rootPolicy(policy("p", Rule.builder("r", Effect.PERMIT).build()).build())
				.decisionCache(cache).observer(recorder).build(), READ);

		assertEquals(Decision.DENY, result.getDecision());
		assertEquals(List.of(
				"requestStarted",
				"request decisionStarted 1",
				"decision1 decisionCacheHit DENY",
				"decision1 decisionFinished DENY",
				"request requestFinished"), recorder.events);
	}

	@Test
	void resolverSpansNestInTheAttributeAndReportFallbackFailureCycleAndCacheHit(){
		AttributeDesignatorKey first = key("first");
		AttributeDesignatorKey second = key("second");
		ResolverRegistry registry = ResolverRegistry.builder()
				.withAttributeResolver(resolver("failing", "role").build(c -> { throw new IllegalStateException("resolver"); }))
				.withAttributeResolver(resolver("empty", "role").build(c -> Map.of()))
				.withAttributeResolver(resolver("giving", "role").build(c -> Map.of("role", bag("admin"))))
				.withAttributeResolver(resolver("first", "first").contextRef(second).build(c -> {
					c.resolveContextRef(second);
					return Map.of("first", bag("1"));
				}))
				.withAttributeResolver(resolver("second", "second").contextRef(first).build(c -> {
					c.resolveContextRef(first);
					return Map.of("second", bag("2"));
				}))
				.build();
		Policy p = policy("p", Rule.builder("r", Effect.PERMIT)
				.condition(isIn("admin", designator("role", CategoryId.SUBJECT_ACCESS, false)))
				.obligation(ObligationExpression.builder("o", Effect.PERMIT).attribute(AttributeAssignmentExpression.builder("first")
						.expression(designator("first", CategoryId.SUBJECT_ACCESS, false)).build()))
				.build()).build();
		Recorder recorder = new Recorder();

		Result result = decide(pdp(p, PolicyInformationPoint.builder("resolvers").registry(registry).build(), recorder), READ);

		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(List.of(
				"r attributeRequested role",
				"attr:role resolverStarted failing",
				"resolver:failing resolverFailed",
				"attr:role resolverStarted empty",
				"resolver:empty resolverFinished absent",
				"attr:role resolverStarted giving",
				"resolver:giving resolverFinished value",
				"attr:role attributeResolved INFORMATION_POINT value",
				"r conditionEvaluated TRUE",
				"r attributeRequested first",
				"attr:first resolverStarted first",
				"resolver:first attributeRequested second",
				"attr:second resolverStarted second",
				"resolver:second attributeRequested first",
				"attr:first attributeResolved CYCLE absent",
				"resolver:second resolverFinished value",
				"attr:second attributeResolved INFORMATION_POINT value",
				"resolver:first resolverFinished value",
				"attr:first attributeResolved INFORMATION_POINT value",
				"r responsesStaged PERMIT 0/1"), recorder.between("r targetMatched MATCH", "r evaluationFinished PERMIT"));

		AttributeResolverDescriptor cached = resolver("cached", "role").cache(30).build(c -> Map.of());
		PolicyInformationPoint cachingPip = PolicyInformationPoint.builder("caching")
				.cacheProvider(new Cached(AttributeSet.builder(cached).clock(Clock.systemUTC())
						.attributes(Map.of("role", bag("admin"))).build()))
				.registry(ResolverRegistry.builder().withAttributeResolver(cached).build())
				.build();
		Recorder hit = new Recorder();

		decide(pdp(policy("p", Rule.builder("r", Effect.PERMIT)
				.condition(isIn("admin", designator("role", CategoryId.SUBJECT_ACCESS, false))).build()).build(), cachingPip, hit), READ);

		assertEquals(List.of(
				"r attributeRequested role",
				"attr:role resolverStarted cached",
				"resolver:cached resolverCacheHit",
				"resolver:cached resolverFinished value",
				"attr:role attributeResolved INFORMATION_POINT value",
				"r conditionEvaluated TRUE"), hit.between("r targetMatched MATCH", "r evaluationFinished PERMIT"));
	}

	/** A path error on the content of a resolver is one mapped resolver failure, for the resolver span and for the attribute (review R-D34-argon) */
	@Test
	void aSelectorPathErrorOnResolvedContentIsOneResolverFailureForTheResolverAndTheAttribute(){
		Content record = Content.fromString("<record><name>x</name></record>", Content.Type.XML_UTF8).orElseThrow();
		PolicyInformationPoint content = PolicyInformationPoint.builder("content").registry(ResolverRegistry.builder()
				.withContentResolver(ContentResolvers.of("content", CategoryId.RESOURCE, c -> record)).build()).build();
		AttributeSelector malformed = AttributeSelector.builder().key(AttributeSelectorKey.builder()
				.category(CategoryId.RESOURCE).xpath("///").dataType(XacmlTypes.STRING).build()).mustBePresent(true).build();
		Recorder recorder = new Recorder();

		Result result = decide(pdp(policy("p", Rule.builder("r", Effect.PERMIT).condition(isIn("x", malformed)).build()).build(),
				content, recorder), READ);

		assertEquals(Decision.INDETERMINATE_P, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE), result.getStatus().getMessage());
		assertEquals(List.of(
				"r attributeRequested ///",
				"attr:/// resolverStarted content",
				"resolver:content resolverFailed",
				"attr:/// attributeFailed"), recorder.between("r targetMatched MATCH", "r conditionEvaluated INDETERMINATE"));
		Throwable failure = recorder.failures.get("resolver:content resolverFailed");
		assertSame(failure, recorder.failures.get("attr:/// attributeFailed"));
		AttributeReferenceEvaluationException mapped = assertInstanceOf(AttributeReferenceEvaluationException.class, failure);
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, mapped.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE), mapped.getStatus().getMessage());
	}

	@Test
	void anObserverExceptionThatTheEngineAbsorbsStillFailsTheDecision(){
		IllegalStateException observer = new IllegalStateException("observer");
		Recorder recorder = new Recorder(){
			@Override
			public void attributeResolved(String attribute, EvaluationContext context, AttributeReferenceKey key,
					AttributeSource source, Optional<BagOfValues> value){
				super.attributeResolved(attribute, context, key, source, value);
				throw observer;
			}
		};
		Policy p = Policy.builder("p").version("1.0")
				.target(target("read", designator("action", CategoryId.ACTION, false)))
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("r", Effect.PERMIT).build())
				.build();
		PolicyDecisionPoint pdp = pdp(set("root", "3.0:deny-overrides").policy(p).build(), roles(), recorder);

		assertSame(observer, assertThrows(IllegalStateException.class, () -> pdp.decide(READ)));

		List<String> events = recorder.events;
		assertEquals(List.of("decision1 decisionFailed observer", "request requestFailed observer"),
				events.subList(events.size() - 2, events.size()));
		assertFalse(events.contains("decision1 decisionFinished INDETERMINATE_P"), () -> "events=" + events);
		// After the exception only the ends of the open spans are reported
		assertEquals(List.of("p evaluationFinished INDETERMINATE_P", "root evaluationFinished INDETERMINATE_P"),
				recorder.between("attr:action attributeResolved REQUEST value", "decision1 decisionFailed observer"));
	}

	@Test
	void anExceptionOfDecisionFinishedIsThrownAfterAuditAndIsNoDecisionFailure(){
		IllegalStateException observer = new IllegalStateException("observer");
		Recorder recorder = new Recorder(){
			@Override
			public void decisionFinished(String decision, Result result){
				super.decisionFinished(decision, result);
				throw observer;
			}
		};
		List<Decision> audited = new ArrayList<>();
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("finishing").policyRepository(PolicyRepository.build("finishing").build())
				.pip(roles()).rootPolicy(policy("p", Rule.builder("r", Effect.PERMIT).build()).build())
				.decisionAuditor((point, result, request) -> audited.add(result.getDecision()))
				.observer(recorder).build();

		assertSame(observer, assertThrows(IllegalStateException.class, () -> pdp.decide(READ)));

		assertEquals(List.of(Decision.PERMIT), audited);
		assertEquals(List.of("decision1 decisionFinished PERMIT", "request requestFailed observer"),
				recorder.events.subList(recorder.events.size() - 2, recorder.events.size()));
		assertFalse(recorder.events.stream().anyMatch(e -> e.contains("decisionFailed")), () -> "events=" + recorder.events);
	}

	@Test
	void aFailureCallbackThatThrowsKeepsTheEngineFailureWithItsExceptionSuppressed(){
		IllegalStateException engine = new IllegalStateException("engine");
		IllegalStateException observer = new IllegalStateException("observer");
		Recorder recorder = new Recorder(){
			@Override
			public void evaluationFailed(String evaluation, EvaluationContext context, DecisionRule rule, Throwable failure){
				super.evaluationFailed(evaluation, context, rule, failure);
				throw observer;
			}
		};
		Policy p = Policy.builder("p").version("1.0")
				.combiningAlgorithm(new DecisionCombiningAlgorithm<Rule>(){
					@Override public String getId(){ return "urn:xacml4j:test:throwing"; }
					@Override public Decision combine(EvaluationContext context, List<Rule> rules){ throw engine; }
				})
				.rule(Rule.builder("r", Effect.PERMIT).build())
				.build();
		PolicyDecisionPoint pdp = pdp(p, roles(), recorder);

		IllegalStateException failure = assertThrows(IllegalStateException.class, () -> pdp.decide(READ));

		assertSame(engine, failure);
		assertEquals(List.of(observer), List.of(failure.getSuppressed()));
		assertEquals(List.of(
				"requestStarted",
				"request decisionStarted 1",
				"decision1 evaluationStarted p",
				"p targetMatched MATCH",
				"p evaluationFailed engine",
				"decision1 decisionFailed engine",
				"request requestFailed engine"), recorder.events);
	}

	/** Records each callback after the token it gets; a token names its span; the failures by their event */
	private static class Recorder implements DecisionObserver<String>
	{
		final List<String> events = new ArrayList<>();
		final Map<String, Throwable> failures = new HashMap<>();

		/** The events after the first one and before the second one */
		List<String> between(String first, String second){
			int from = events.indexOf(first);
			return events.subList(from + 1, events.subList(from, events.size()).indexOf(second) + from);
		}

		@Override
		public String requestStarted(PolicyDecisionPoint pdp, RequestContext request){
			events.add("requestStarted");
			return "request";
		}

		@Override
		public void requestFinished(String request, ResponseContext response){
			events.add(request + " requestFinished");
		}

		@Override
		public void requestFailed(String request, Throwable failure){
			events.add(request + " requestFailed " + failure.getMessage());
		}

		@Override
		public String decisionStarted(String request, RequestContext individualRequest, long correlation, int number){
			events.add(request + " decisionStarted " + number);
			return "decision" + number;
		}

		@Override
		public void decisionCacheHit(String decision, Result result){
			events.add(decision + " decisionCacheHit " + result.getDecision());
		}

		@Override
		public void decisionFinished(String decision, Result result){
			events.add(decision + " decisionFinished " + result.getDecision());
		}

		@Override
		public void decisionFailed(String decision, Throwable failure){
			events.add(decision + " decisionFailed " + failure.getMessage());
		}

		@Override
		public String evaluationStarted(String parent, EvaluationContext context, DecisionRule rule){
			events.add(parent + " evaluationStarted " + name(rule));
			return name(rule);
		}

		@Override
		public void evaluationFinished(String evaluation, EvaluationContext context, DecisionRule rule, Decision decision){
			events.add(evaluation + " evaluationFinished " + decision);
		}

		@Override
		public void evaluationFailed(String evaluation, EvaluationContext context, DecisionRule rule, Throwable failure){
			events.add(evaluation + " evaluationFailed " + failure.getMessage());
		}

		@Override
		public String extendedIndeterminateStarted(String evaluation, EvaluationContext context, CompositeDecisionRule rule){
			events.add(evaluation + " passStarted");
			return "pass:" + rule.getId();
		}

		@Override
		public void extendedIndeterminateFinished(String pass, EvaluationContext context, CompositeDecisionRule rule, Decision decision){
			events.add(pass + " passFinished " + decision);
		}

		@Override
		public String attributeRequested(String parent, EvaluationContext context, AttributeReferenceKey key){
			events.add(parent + " attributeRequested " + id(key));
			return "attr:" + id(key);
		}

		@Override
		public void attributeResolved(String attribute, EvaluationContext context, AttributeReferenceKey key,
				AttributeSource source, Optional<BagOfValues> value){
			events.add(attribute + " attributeResolved " + source + (value.isPresent() ? " value" : " absent"));
		}

		@Override
		public void attributeFailed(String attribute, EvaluationContext context, AttributeReferenceKey key, Throwable failure){
			events.add(attribute + " attributeFailed");
			failures.put(attribute + " attributeFailed", failure);
		}

		@Override
		public String resolverStarted(String parent, EvaluationContext context, String resolverId, AttributeReferenceKey key){
			events.add(parent + " resolverStarted " + resolverId);
			return "resolver:" + resolverId;
		}

		@Override
		public void resolverCacheHit(String resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key){
			events.add(resolver + " resolverCacheHit");
		}

		@Override
		public void resolverFinished(String resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
				Optional<BagOfValues> value){
			events.add(resolver + " resolverFinished" + (value.isPresent() ? " value" : " absent"));
		}

		@Override
		public void resolverFailed(String resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
				Throwable failure){
			events.add(resolver + " resolverFailed");
			failures.put(resolver + " resolverFailed", failure);
		}

		@Override
		public void targetMatched(String current, EvaluationContext context, DecisionRule rule, MatchResult result){
			events.add(current + " targetMatched " + result);
		}

		@Override
		public void conditionEvaluated(String current, EvaluationContext context, DecisionRule rule, ConditionResult result){
			events.add(current + " conditionEvaluated " + result);
		}

		@Override
		public void childrenSelected(String current, EvaluationContext context, CompositeDecisionRule rule,
				List<? extends DecisionRule> selected){
			events.add(current + " childrenSelected " + selected.size());
		}

		@Override
		public void functionFailed(String current, EvaluationContext context, FunctionSpec function, Throwable failure){
			events.add(current + " functionFailed " + function.getShortId());
		}

		@Override
		public void responsesStaged(String current, EvaluationContext context, DecisionRule rule, Decision decision,
				List<Advice> advice, List<Obligation> obligations){
			events.add(current + " responsesStaged " + decision + " " + advice.size() + "/" + obligations.size());
		}

		private static String name(DecisionRule rule){
			return (rule instanceof CompositeDecisionRuleIDReference ? "ref:" : "") + rule.getId();
		}

		/** A designator by its attribute id, a selector by its path */
		private static String id(AttributeReferenceKey key){
			return key instanceof AttributeDesignatorKey designator
					? designator.getAttributeId() : ((AttributeSelectorKey) key).getPath();
		}
	}

	/** A cache of the information point that holds one attribute set */
	private static final class Cached extends BasePolicyInformationPointCacheProvider
	{
		private final AttributeSet set;

		Cached(AttributeSet set){
			this.set = set;
		}

		@Override
		protected ContentRef doGetContent(ResolverContext context){
			return null;
		}

		@Override
		protected AttributeSet doGetAttributes(ResolverContext context){
			return set;
		}

		@Override
		protected void doPutContent(ResolverContext context, ContentRef content){
		}

		@Override
		protected void doPutAttributes(ResolverContext context, AttributeSet v){
		}
	}

	private static Result decide(PolicyDecisionPoint pdp, RequestContext request){
		return pdp.decide(request).getResults().iterator().next();
	}

	private static PolicyDecisionPoint pdp(CompositeDecisionRule root, PolicyInformationPoint pip, DecisionObserver<?> observer){
		return PolicyDecisionPointBuilder.builder("observed").policyRepository(PolicyRepository.build("observed").build())
				.pip(pip).rootPolicy(root).observer(observer).build();
	}

	/** An information point that gives the role attribute */
	private static PolicyInformationPoint roles(){
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		when(pip.resolve(any(EvaluationContext.class), any(AttributeDesignatorKey.class))).thenAnswer(call -> {
			AttributeDesignatorKey key = call.getArgument(1);
			return key.getAttributeId().equals("role") ? Optional.of(bag("admin")) : Optional.empty();
		});
		return pip;
	}

	private static AttributeResolverDescriptor.Builder resolver(String id, String attribute){
		return AttributeResolverDescriptor.builder(id, id, CategoryId.SUBJECT_ACCESS).noCache().attribute(attribute, XacmlTypes.STRING);
	}

	private static BagOfValues bag(String value){
		return XacmlTypes.STRING.bagOf(StringVal.of(value));
	}

	private static AttributeDesignatorKey key(String id){
		return AttributeDesignatorKey.builder(id).category(CategoryId.SUBJECT_ACCESS).dataType(XacmlTypes.STRING).build();
	}

	private static AttributeAssignmentExpression role(){
		return AttributeAssignmentExpression.builder("role").expression(designator("role", CategoryId.SUBJECT_ACCESS, true)).build();
	}

	private static AttributeDesignator designator(String id, CategoryId category, boolean mustBePresent){
		return AttributeDesignator.builder().key(AttributeDesignatorKey.builder(id).category(category)
				.dataType(XacmlTypes.STRING).build()).mustBePresent(mustBePresent).build();
	}

	private static Target target(String value, AttributeDesignator designator){
		return Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of(value), designator, function("1.0:function:string-equal"))
				.build()).build()).build();
	}

	private static Condition isIn(String value, AttributeReference reference){
		return Condition.condition(Apply.builder(function("1.0:function:string-is-in"))
				.param(StringVal.of(value), reference).build());
	}

	private static FunctionSpec function(String id){
		return FUNCTIONS.getFunction(URN + id).orElseThrow();
	}

	private static Policy.Builder policy(String id, Rule rule){
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(rule);
	}

	private static PolicySet.Builder set(String id, String algorithm){
		String[] parts = algorithm.split(":", 2);
		return PolicySet.builder(id).version("1.0").withCombiningAlgorithm(
				ALGORITHMS.getPolicyAlgorithm(URN + parts[0] + ":policy-combining-algorithm:" + parts[1]));
	}
}
