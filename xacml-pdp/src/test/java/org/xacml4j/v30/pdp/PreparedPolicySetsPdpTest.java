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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.codahale.metrics.MetricRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.content.XPathProvider;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.audit.NoAuditPolicyDecisionPointAuditor;
import org.xacml4j.v30.spi.pdp.NoCachePolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.DefaultPolicyReferenceResolver;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.AbstractPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

class PreparedPolicySetsPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final AttributeDesignatorKey HOOK = AttributeDesignatorKey.builder("hook")
			.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build();
	private static final RequestContext REQUEST = RequestContext.builder().returnPolicyIdList(true).build();

	static Stream<Arguments> shortCircuits(){
		List<Arguments> cases = new ArrayList<>();
		for(String failure : List.of("runtime", "error", "resolution", "missing")){
			for(String algorithm : List.of("3.0:deny-overrides", "3.0:ordered-deny-overrides",
					"1.0:deny-overrides", "1.1:ordered-deny-overrides", "3.0:permit-unless-deny")){
				cases.add(Arguments.of(algorithm, Effect.DENY, "ordinary", failure));
			}
			for(String algorithm : List.of("3.0:permit-overrides", "3.0:ordered-permit-overrides",
					"1.0:permit-overrides", "1.1:ordered-permit-overrides", "3.0:deny-unless-permit",
					"1.0:first-applicable")){
				cases.add(Arguments.of(algorithm, Effect.PERMIT, "ordinary", failure));
			}
			cases.add(Arguments.of("1.0:only-one-applicable", Effect.PERMIT, "two-matches", failure));
			cases.add(Arguments.of("1.0:only-one-applicable", Effect.PERMIT, "indeterminate", failure));
			cases.add(Arguments.of("3.0:on-permit-apply-second", Effect.PERMIT, "second", failure));
			cases.add(Arguments.of("3.0:on-permit-apply-second", Effect.DENY, "third", failure));
		}
		return cases.stream().flatMap(item -> Stream.of(false, true).map(index -> {
			Object[] values = item.get();
			return Arguments.of(values[0], values[1], values[2], values[3], index);
		}));
	}

	@ParameterizedTest
	@MethodSource("shortCircuits")
	void anUnvisitedFailureDoesNotChangeTheResponse(String algorithm, Effect effect, String shape, String failure, boolean index){
		CompositeDecisionRule first = policy("first", effect);
		CompositeDecisionRule second = policy("second", Effect.PERMIT);
		CompositeDecisionRule bad = PolicyIDReference.builder("bad").build();
		List<CompositeDecisionRule> children = switch(shape){
			case "two-matches", "second" -> List.of(first, second, bad);
			case "indeterminate" -> List.of(PolicyIDReference.builder("missing").build(), bad);
			case "third" -> List.of(first, bad, second);
			default -> List.of(first, bad);
		};
		PolicySet root = set("root", algorithm, children);
		assertResponse(result(pdp(root, repository(failure), false)),
				result(pdp(root, repository(failure), mock(PolicyInformationPoint.class), true, index)));
	}

	@ParameterizedTest
	@MethodSource("escapingFailures")
	void visitedFailuresEscapeAsTheyDoInTheBaseline(String failure, Class<? extends Throwable> type){
		PolicySet root = set("root", "1.0:first-applicable", List.of(PolicyIDReference.builder("bad").build()));
		Throwable baseline = assertThrows(type, () -> result(pdp(root, repository(failure), false)));
		Throwable prepared = assertThrows(type, () -> result(pdp(root, repository(failure), true)));
		assertEquals(errorChain(baseline), errorChain(prepared));
	}

	static Stream<Arguments> escapingFailures(){
		return Stream.of(Arguments.of("runtime", IllegalStateException.class), Arguments.of("error", AssertionError.class));
	}

	@Test
	void aVisitedMissAndMismatchKeepTheBaselineResponse(){
		PolicySet root = set("root", "1.0:first-applicable", List.of(PolicyIDReference.builder("bad").build()));
		assertResponse(result(pdp(root, repository("missing"), false)), result(pdp(root, repository("missing"), true)));
		assertResponse(result(pdp(root, mismatchingRepository(), false)), result(pdp(root, mismatchingRepository(), true)));
	}

	@Test
	void aMismatchDoesNotHideAValidPolicyFromTheNextDecision(){
		PolicySet root = set("root", "1.0:first-applicable", List.of(PolicyIDReference.builder("bad").build()));
		PolicyDecisionPoint prepared = pdp(root, mismatchingRepository(), true);
		Result first = result(prepared);
		assertResponse(result(pdp(root, mismatchingRepository(), false)), first);
		assertTrue(first.getDecision().isIndeterminate());
		assertEquals(Decision.PERMIT, result(prepared).getDecision());
	}

	@Test
	void aMissIsReplayedDuringTheDecisionAndRetriedOnTheNextDecision(){
		PolicyRepository repository = mock(PolicyRepository.class);
		when(repository.getPolicy("bad", null, null, null)).thenReturn(null, policy("bad", Effect.PERMIT));
		PolicySet root = set("root", "1.0:first-applicable", List.of(PolicyIDReference.builder("bad").build()));
		PolicyDecisionPoint prepared = pdp(root, repository, true);
		assertTrue(result(prepared).getDecision().isIndeterminate());
		assertEquals(Decision.PERMIT, result(prepared).getDecision());
	}

	@Test
	void aResolutionFailureIsReplayedDuringTheDecisionAndRetriedOnTheNextDecision(){
		PolicyRepository repository = mock(PolicyRepository.class);
		when(repository.getPolicy("bad", null, null, null))
				.thenThrow(new PolicyResolutionException(null)).thenReturn(policy("bad", Effect.PERMIT));
		PolicySet root = set("root", "1.0:first-applicable", List.of(PolicyIDReference.builder("bad").build()));
		PolicyDecisionPoint prepared = pdp(root, repository, true);
		assertTrue(result(prepared).getDecision().isIndeterminate());
		assertEquals(Decision.PERMIT, result(prepared).getDecision());
	}

	@Test
	void resolverReturnsTheCorrectedPolicyAfterAMismatch(){
		PolicyRepository repository = mismatchingRepository();
		DefaultPolicyReferenceResolver resolver = new DefaultPolicyReferenceResolver(repository);
		PolicyIDReference ref = PolicyIDReference.builder("bad").build();
		assertEquals("wrong", resolver.resolve(ref).getId());
		assertEquals("bad", resolver.resolve(ref).getId());
	}

	@Test
	void resolverReturnsTheCorrectedPolicySetAfterAMismatch(){
		PolicyRepository repository = mock(PolicyRepository.class);
		when(repository.getPolicySet("bad", null, null, null)).thenReturn(
				set("wrong", "1.0:first-applicable", List.of(policy("nested", Effect.PERMIT))),
				set("bad", "1.0:first-applicable", List.of(policy("nested", Effect.PERMIT))));
		DefaultPolicyReferenceResolver resolver = new DefaultPolicyReferenceResolver(repository);
		PolicySetIDReference ref = PolicySetIDReference.builder("bad").build();
		assertEquals("wrong", resolver.resolve(ref).getId());
		assertEquals("bad", resolver.resolve(ref).getId());
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void aDecisionKeepsItsTargetAcrossARepositoryChangeAndACorrectedColdSet(boolean asynchronous) throws Exception {
		try(ExecutorService executor = Executors.newFixedThreadPool(2)){
			Policy old = versionedPolicy("1.0", "old");
			Policy replacement = versionedPolicy("2.0", "new");
			InMemoryPolicyRepository repository = new InMemoryPolicyRepository("snapshot", FUNCTIONS, ALGORITHMS);
			setExecutor(repository, asynchronous ? executor : Runnable::run);
			repository.add(old);
			PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
			AtomicBoolean changed = new AtomicBoolean();
			when(pip.resolve(any(EvaluationContext.class), eq(HOOK))).thenAnswer(call -> {
				if(changed.compareAndSet(false, true)){
					repository.add(replacement);
					if(asynchronous){
						executor.shutdown();
						if(!executor.awaitTermination(10, TimeUnit.SECONDS)) throw new IllegalStateException("listeners did not finish");
					}
				}
				return Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("go")));
			});
			PolicyIDReference ref = PolicyIDReference.builder("shared").build();
			PolicySet nested = set("cold", "1.0:first-applicable", List.of(ref));
			PolicySet root = set("root", "3.0:deny-overrides", List.of(ref, hookPolicy(), nested));
			PolicyDecisionPoint prepared = pdp(root, repository, pip, true);
			Result first = result(prepared);
			assertEquals(Decision.PERMIT, first.getDecision());
			assertTrue(ids(first.getObligations()).contains("old-obligation"));
			assertTrue(!ids(first.getObligations()).contains("new-obligation"));
			Result next = result(prepared);
			assertEquals(Decision.PERMIT, next.getDecision());
			assertTrue(ids(next.getObligations()).contains("new-obligation"));
			assertTrue(!ids(next.getObligations()).contains("old-obligation"));
			assertResponse(result(pdp(root, repository, pip, false)), next);
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void aMissInOneDecisionOverridesAPinPublishedByAnotherDecision(boolean index){
		PolicyRepository repository = mock(PolicyRepository.class);
		when(repository.getPolicy("shared", null, null, null)).thenReturn(null, nonMatchingPolicy());
		PolicyIDReference ref = PolicyIDReference.builder("shared").build();
		PolicySet first = set("first", "3.0:permit-unless-deny", List.of(ref));
		PolicySet second = set("second", "3.0:deny-overrides", List.of(ref));
		PolicySet root = set("root", "3.0:deny-overrides", List.of(first, hookPolicy(), second));
		PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
		RequestContext request = RequestContext.builder().returnPolicyIdList(true).attributes(Category.resource()
				.entity(Entity.builder().attribute(Attribute.builder("other").value(StringVal.of("different")).build()).build())
				.build()).build();
		AtomicReference<PolicyDecisionPoint> pdp = new AtomicReference<>();
		AtomicBoolean nested = new AtomicBoolean();
		when(pip.resolve(any(EvaluationContext.class), eq(HOOK))).thenAnswer(call -> {
			if(nested.compareAndSet(false, true)) assertEquals(Decision.PERMIT, result(pdp.get(), request).getDecision());
			return Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("go")));
		});
		pdp.set(pdp(root, repository, pip, true, index));
		assertTrue(result(pdp.get(), request).getDecision().isIndeterminate());
	}

	static Stream<Arguments> repositoryEvents(){
		return Stream.of("policy-added", "policy-removed", "set-added", "set-removed")
				.flatMap(event -> Stream.of(Arguments.of(event, false), Arguments.of(event, true)));
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void closingAPdpStopsRepositoryChangesReachingIt(boolean prepared) throws Exception {
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("close", FUNCTIONS, ALGORITHMS);
		setExecutor(repository, Runnable::run);
		repository.add(policyVersion("1.0", Effect.PERMIT));
		PolicySet root = set("root", "1.0:first-applicable", List.of(PolicyIDReference.builder("entry").build()));
		PolicyDecisionPoint closed = pdp(root, repository, prepared);
		try(PolicyDecisionPoint active = pdp(root, repository, prepared)){
			assertEquals(Decision.PERMIT, result(closed).getDecision());
			assertEquals(Decision.PERMIT, result(active).getDecision());
			closed.close();
			closed.close();
			repository.add(policyVersion("2.0", Effect.DENY));
			assertEquals(Decision.PERMIT, result(closed).getDecision());
			assertEquals(Decision.DENY, result(active).getDecision());
		}
	}

	@ParameterizedTest
	@MethodSource("repositoryEvents")
	void repositoryChangesMatchAFreshPdp(String event, boolean asynchronous) throws Exception {
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("events", FUNCTIONS, ALGORITHMS);
		boolean set = event.startsWith("set");
		boolean remove = event.endsWith("removed");
		CompositeDecisionRule old = set ? versionedSet("1.0", Effect.PERMIT) : policyVersion("1.0", Effect.PERMIT);
		CompositeDecisionRule next = set ? versionedSet("2.0", Effect.DENY) : policyVersion("2.0", Effect.DENY);
		repository.add(old);
		if(remove) repository.add(next);
		CompositeDecisionRule reference = set ? PolicySetIDReference.builder("entry").build()
				: PolicyIDReference.builder("entry").build();
		PolicySet root = set("root", "1.0:first-applicable", List.of(reference));
		PolicyDecisionPoint prepared = pdp(root, repository, true);
		assertEquals(remove ? Decision.DENY : Decision.PERMIT, result(prepared).getDecision());
		if(asynchronous){
			try(ExecutorService executor = Executors.newFixedThreadPool(2)){
				setExecutor(repository, executor);
				if(remove) repository.remove(next); else repository.add(next);
				executor.shutdown();
				assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
			}
		}else{
			setExecutor(repository, Runnable::run);
			if(remove) repository.remove(next); else repository.add(next);
		}
		assertResponse(result(pdp(root, repository, false)), result(prepared));
	}

	private static Policy policyVersion(String version, Effect effect){
		return Policy.builder("entry").version(version)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("rule", effect).build()).build();
	}

	private static PolicySet versionedSet(String version, Effect effect){
		return PolicySet.builder("entry").version(version)
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + "1.0:policy-combining-algorithm:first-applicable"))
				.policy(policy("nested", effect)).build();
	}

	private static void setExecutor(InMemoryPolicyRepository repository, java.util.concurrent.Executor executor) throws Exception {
		java.lang.reflect.Field field = AbstractPolicyRepository.class.getDeclaredField("notificationExecutor");
		field.setAccessible(true);
		field.set(repository, executor);
	}

	private static Policy versionedPolicy(String version, String origin){
		return Policy.builder("shared").version(version)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(origin, Effect.PERMIT).build())
				.obligation(ObligationExpression.builder(origin + "-obligation", Effect.PERMIT).attribute("origin", StringVal.of(origin)))
				.build();
	}

	private static Policy hookPolicy(){
		AttributeDesignator hook = AttributeDesignator.builder().key(HOOK).mustBePresent(true).build();
		return Policy.builder("hook-policy").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("hook-rule", Effect.PERMIT).condition(new Condition(
						Apply.builder(URN + "1.0:function:string-is-in", FUNCTIONS::getFunction)
								.param(StringVal.of("go"), hook).build())).build()).build();
	}

	private static Policy nonMatchingPolicy(){
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("other")
				.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build();
		AttributeDesignator ref = AttributeDesignator.builder().key(key).mustBePresent(false).build();
		Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
				.match(StringVal.of("wanted"), ref, FUNCTIONS.getFunction(URN + "1.0:function:string-equal").orElseThrow())
				.build()).build()).build();
		return Policy.builder("shared").version("1.0").target(target)
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("rule", Effect.PERMIT).build()).build();
	}

	private static List<String> ids(Collection<? extends DecisionRuleResponse> responses){
		return responses.stream().map(DecisionRuleResponse::getId).toList();
	}

	private static PolicyRepository repository(String failure){
		PolicyRepository repository = mock(PolicyRepository.class);
		switch(failure){
			case "runtime" -> when(repository.getPolicy("bad", null, null, null))
					.thenThrow(new IllegalStateException("backend", new IllegalArgumentException("cause")));
			case "error" -> when(repository.getPolicy("bad", null, null, null))
					.thenThrow(new AssertionError("backend", new IllegalArgumentException("cause")));
			case "resolution" -> when(repository.getPolicy("bad", null, null, null))
					.thenThrow(new PolicyResolutionException(null));
			default -> { }
		}
		return repository;
	}

	private static PolicyRepository mismatchingRepository(){
		PolicyRepository repository = mock(PolicyRepository.class);
		when(repository.getPolicy("bad", null, null, null)).thenReturn(policy("wrong", Effect.PERMIT), policy("bad", Effect.PERMIT));
		return repository;
	}

	private static PolicyDecisionPoint pdp(PolicySet root, PolicyRepository repository, boolean prepared){
		return pdp(root, repository, mock(PolicyInformationPoint.class), prepared);
	}

	private static PolicyDecisionPoint pdp(PolicySet root, PolicyRepository repository, PolicyInformationPoint pip, boolean prepared){
		return pdp(root, repository, pip, prepared, false);
	}

	private static PolicyDecisionPoint pdp(PolicySet root, PolicyRepository repository, PolicyInformationPoint pip,
			boolean prepared, boolean index){
		if(prepared){
			PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("prepared").rootPolicy(root)
					.policyRepository(repository).pip(pip);
			if(index) builder.targetIndex();
			return builder.build();
		}
		return new DefaultPolicyDecisionPoint("baseline", new MetricRegistry(),
				new PolicyDecisionPointContextFactory.DefaultPolicyDecisionPointContextFactory(root, repository,
						new NoAuditPolicyDecisionPointAuditor(), new NoCachePolicyDecisionCache(), XPathProvider.defaultProvider(),
						pip, new RequestContextHandlerChain(List.of())));
	}

	private static Result result(PolicyDecisionPoint pdp){
		return pdp.decide(REQUEST).getResults().iterator().next();
	}

	private static Result result(PolicyDecisionPoint pdp, RequestContext request){
		return pdp.decide(request).getResults().iterator().next();
	}

	private static Policy policy(String id, Effect effect){
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(id + "-rule", effect).build())
				.obligation(ObligationExpression.builder(id + "-obligation", effect).attribute("origin", StringVal.of(id)))
				.advice(AdviceExpression.builder(id + "-advice", effect).attribute("origin", StringVal.of(id)))
				.build();
	}

	private static PolicySet set(String id, String algorithm, List<CompositeDecisionRule> children){
		String[] parts = algorithm.split(":", 2);
		return PolicySet.builder(id).version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(URN + parts[0] + ":policy-combining-algorithm:" + parts[1]))
				.compositeDecisionRules(children).build();
	}

	private static void assertResponse(Result expected, Result actual){
		assertEquals(expected, actual);
		assertEquals(expected.getStatus().getError().map(PreparedPolicySetsPdpTest::errorChain).orElse(List.of()),
				actual.getStatus().getError().map(PreparedPolicySetsPdpTest::errorChain).orElse(List.of()));
		assertResponses(expected.getObligations(), actual.getObligations());
		assertResponses(expected.getAssociatedAdvice(), actual.getAssociatedAdvice());
		assertEquals(List.copyOf(expected.getPolicyIdentifiers()), List.copyOf(actual.getPolicyIdentifiers()));
	}

	private static void assertResponses(Collection<? extends DecisionRuleResponse> expected,
			Collection<? extends DecisionRuleResponse> actual){
		List<? extends DecisionRuleResponse> a = List.copyOf(actual);
		List<? extends DecisionRuleResponse> e = List.copyOf(expected);
		assertEquals(e, a);
		for(int i = 0; i < e.size(); i++){
			assertEquals(e.get(i).getFulfillOn(), a.get(i).getFulfillOn());
			assertEquals(List.copyOf(e.get(i).getAttributes()), List.copyOf(a.get(i).getAttributes()));
		}
	}

	private static List<String> errorChain(Throwable error){
		List<String> chain = new ArrayList<>();
		for(Throwable cause = error; cause != null; cause = cause.getCause()){
			chain.add(cause.getClass().getName() + ":" + cause.getMessage());
		}
		return chain;
	}
}
