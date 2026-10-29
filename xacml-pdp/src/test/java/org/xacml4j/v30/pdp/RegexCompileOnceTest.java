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
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.AttributeDesignator;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.MatchAllOf;
import org.xacml4j.v30.policy.MatchAnyOf;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.Target;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;
import org.xacml4j.v30.policy.function.regex.CompiledRegex;
import org.xacml4j.v30.policy.function.regex.RegexBudget;
import org.xacml4j.v30.policy.function.regex.RegexCompileException;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexLimitException;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;
import org.xacml4j.v30.policy.function.regex.jdk.JdkRegexEngine;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Compiled patterns are reused (change add-regex-engine-spi, design D6, maintainer decision 4 of
 * 2026-10-28): a literal pattern of a {@code Match} or an {@code Apply} is compiled once at its
 * call site, every other pattern through a bounded cache per engine instance, and only a
 * deterministic failure is kept. Every test reads the outcome of decisions: an engine double that
 * compiles a pattern once, and rejects it as malformed after that, turns every second compilation
 * into an Indeterminate decision.
 */
class RegexCompileOnceTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:1.0:";
	private static final String STRING_REGEXP_MATCH = URN + "function:string-regexp-match";
	private static final String STRING_ONE_AND_ONLY = URN + "function:string-one-and-only";
	private static final String SUBJECT_ID = URN + "subject:subject-id";
	private static final String PATTERN = "urn:example:pattern";
	private static final String LITERAL = "^user-[0-9]+$";
	private static final String COMPILED_AGAIN = "compiled again";

	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();

	/** Conformance case IIC168 (OASIS XACML 3.0 suite): patterns reach string-regexp-match through any-of-all and a bag */
	private static final String IIC168_POLICY = """
			<Policy xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17"
			        PolicyId="urn:oasis:names:tc:xacml:2.0:conformance-test:IIC168:policy" Version="1.0"
			        RuleCombiningAlgId="urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides">
			    <Target/>
			    <Rule Effect="Permit" RuleId="urn:oasis:names:tc:xacml:2.0:conformance-test:IIC168:rule">
			        <Condition>
			            <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:any-of-all">
			                <Function FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"/>
			                <Apply FunctionId="urn:oasis:names:tc:xacml:1.0:function:string-bag">
			                    <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">   This  is n*o*t* *IT!  </AttributeValue>
			                    <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">   This is not a match to IT!  </AttributeValue>
			                </Apply>
			                <AttributeDesignator AttributeId="urn:oasis:names:tc:xacml:2.0:conformance-test:test-attr"
			                        Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
			                        DataType="http://www.w3.org/2001/XMLSchema#string" MustBePresent="false"/>
			            </Apply>
			        </Condition>
			    </Rule>
			</Policy>
			""";

	private static final String IIC168_REQUEST = """
			<Request xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" ReturnPolicyIdList="false" CombinedDecision="false">
			  <Attributes Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject">
			    <Attribute IncludeInResult="false" AttributeId="urn:oasis:names:tc:xacml:1.0:subject:subject-id">
			      <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">Julius Hibbert</AttributeValue>
			    </Attribute>
			    <Attribute IncludeInResult="false" AttributeId="urn:oasis:names:tc:xacml:2.0:conformance-test:test-attr">
			      <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">   This  is IT!  </AttributeValue>
			    </Attribute>
			    <Attribute IncludeInResult="false" AttributeId="urn:oasis:names:tc:xacml:2.0:conformance-test:test-attr">
			      <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">   This  is not IT!  </AttributeValue>
			    </Attribute>
			  </Attributes>
			  <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
			    <Attribute IncludeInResult="false" AttributeId="urn:oasis:names:tc:xacml:1.0:resource:resource-id">
			      <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#anyURI">http://medico.com/record/patient/BartSimpson</AttributeValue>
			    </Attribute>
			  </Attributes>
			  <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:action">
			    <Attribute IncludeInResult="false" AttributeId="urn:oasis:names:tc:xacml:1.0:action:action-id">
			      <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">read</AttributeValue>
			    </Attribute>
			  </Attributes>
			  <Attributes Category="urn:oasis:names:tc:xacml:3.0:attribute-category:environment"/>
			</Request>
			""";

	/** Spec scenario "Literal pattern in a target": 1,000 requests, one compilation; the call site keeps it through cache churn */
	@Test
	void aLiteralPatternOfATargetIsCompiledOnceAtItsCallSite() throws Exception
	{
		Fixture fixture = new Fixture(new CompilesOnce(), 2);
		try(PolicyDecisionPoint target = fixture.pdp("target", fixture.targetPolicy(LITERAL));
				PolicyDecisionPoint dynamic = fixture.pdp("dynamic", fixture.dynamicPolicy())){
			for(int i = 0; i < 1_000; i++){
				assertEquals(Decision.PERMIT, decide(target, subject("user-" + i)).getDecision(), "request " + i);
			}
			churnTheCache(dynamic);
			assertEquals(Decision.PERMIT, decide(target, subject("user-1000")).getDecision());
			assertEquals(Decision.NOT_APPLICABLE, decide(target, subject("guest")).getDecision());
		}
	}

	/** The same for the literal first argument of an Apply, here the condition of a rule */
	@Test
	void aLiteralPatternOfAnApplyIsCompiledOnceAtItsCallSite() throws Exception
	{
		Fixture fixture = new Fixture(new CompilesOnce(), 2);
		try(PolicyDecisionPoint condition = fixture.pdp("condition", fixture.conditionPolicy(LITERAL));
				PolicyDecisionPoint dynamic = fixture.pdp("dynamic", fixture.dynamicPolicy())){
			for(int i = 0; i < 1_000; i++){
				assertEquals(Decision.PERMIT, decide(condition, subject("user-" + i)).getDecision(), "request " + i);
			}
			churnTheCache(dynamic);
			assertEquals(Decision.PERMIT, decide(condition, subject("user-1000")).getDecision());
			assertEquals(Decision.NOT_APPLICABLE, decide(condition, subject("guest")).getDecision());
		}
	}

	/** Spec scenario "Higher-order call": the second evaluation of IIC168 compiles nothing and decides the same */
	@Test
	void thePatternsOfTheHigherOrderPathAreCompiledOnce() throws Exception
	{
		Fixture fixture = new Fixture(new CompilesOnce(), RegexLimits.DEFAULT_CACHE_SIZE);
		CompositeDecisionRule root = fixture.repository.newImportTool().importPolicy(MediaType.Type.XACML30_XML,
				() -> new ByteArrayInputStream(IIC168_POLICY.getBytes(StandardCharsets.UTF_8)));
		RequestContext request = MarshallingProvider.getProvider(MediaType.Type.XACML30_XML).orElseThrow()
				.newRequestUnmarshaller().orElseThrow()
				.unmarshal(new ByteArrayInputStream(IIC168_REQUEST.getBytes(StandardCharsets.UTF_8)));
		try(PolicyDecisionPoint pdp = fixture.pdp("IIC168", root)){
			// IIC168Response.xml: Permit
			Result first = decide(pdp, request);
			Result second = decide(pdp, request);

			assertEquals(Decision.PERMIT, first.getDecision());
			assertEquals(first, second);
		}
	}

	/**
	 * Spec scenario "Deterministic failure kept": a malformed pattern is not compiled again, at a call
	 * site or in the cache; a second compilation would succeed and permit. The status of a failing
	 * target match is checked by its decision only: a rule records it per rule and the result
	 * reports a processing-error without a message, before and after this change (pre-existing,
	 * reported with change add-regex-engine-spi group 5).
	 */
	@Test
	void aDeterministicFailureIsKept() throws Exception
	{
		Fixture fixture = new Fixture(new FailsFirst(() -> RegexCompileException.malformed("injected")), 2);
		try(PolicyDecisionPoint target = fixture.pdp("target", fixture.targetPolicy(LITERAL));
				PolicyDecisionPoint condition = fixture.pdp("condition", fixture.conditionPolicy("^guest-[0-9]+$"));
				PolicyDecisionPoint dynamic = fixture.pdp("dynamic", fixture.dynamicPolicy())){
			assertEquals(Decision.INDETERMINATE_P, decide(target, subject("user-1")).getDecision());
			assertEquals(Decision.INDETERMINATE_P, decide(target, subject("user-2")).getDecision());
			assertMalformed(decide(condition, subject("guest-1")));
			assertMalformed(decide(condition, subject("guest-2")));
			assertMalformed(decide(dynamic, subject("user-1", "abc")));
			assertMalformed(decide(dynamic, subject("user-2", "abc")));
		}
	}

	/**
	 * Spec scenario "Transient failure is retried": a deadline and a resource failure are not kept;
	 * the next evaluation compiles again and permits. The target match is checked by its decision
	 * only, as in {@link #aDeterministicFailureIsKept}.
	 */
	@Test
	void aTransientFailureIsRetried() throws Exception
	{
		List<Supplier<RuntimeException>> transients = List.of(
				() -> new RegexLimitException(RegexBudget.LIMIT_EXCEEDED_MESSAGE),
				() -> RegexCompileException.limit(new StackOverflowError("injected")));
		for(Supplier<RuntimeException> failure : transients){
			Fixture fixture = new Fixture(new FailsFirst(failure), 2);
			try(PolicyDecisionPoint target = fixture.pdp("target", fixture.targetPolicy(LITERAL));
					PolicyDecisionPoint condition = fixture.pdp("condition", fixture.conditionPolicy("^guest-[0-9]+$"));
					PolicyDecisionPoint dynamic = fixture.pdp("dynamic", fixture.dynamicPolicy())){
				assertEquals(Decision.INDETERMINATE_P, decide(target, subject("user-1")).getDecision());
				assertEquals(Decision.PERMIT, decide(target, subject("user-2")).getDecision());
				assertLimit(decide(condition, subject("guest-1")));
				assertEquals(Decision.PERMIT, decide(condition, subject("guest-2")).getDecision());
				assertLimit(decide(dynamic, subject("user-1", "^user-1$")));
				assertEquals(Decision.PERMIT, decide(dynamic, subject("user-1", "^user-1$")).getDecision());
			}
		}
	}

	/** Threads that miss the same pattern at once share one compilation */
	@Test
	void concurrentMissesCompileOnce() throws Exception
	{
		Fixture fixture = new Fixture(new CompilesOnce(), RegexLimits.DEFAULT_CACHE_SIZE);
		int threads = 8;
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		try(PolicyDecisionPoint target = fixture.pdp("target", fixture.targetPolicy(LITERAL));
				PolicyDecisionPoint dynamic = fixture.pdp("dynamic", fixture.dynamicPolicy())){
			for(PolicyDecisionPoint pdp : List.of(target, dynamic)){
				CountDownLatch start = new CountDownLatch(1);
				List<Callable<Decision>> calls = new ArrayList<>();
				for(int t = 0; t < threads; t++){
					RequestContext request = subject("user-" + t, "^user-[0-9]$");
					calls.add(() -> {
						start.await();
						return decide(pdp, request).getDecision();
					});
				}
				List<Future<Decision>> futures = new ArrayList<>();
				for(Callable<Decision> call : calls){
					futures.add(executor.submit(call));
				}
				start.countDown();
				for(Future<Decision> future : futures){
					assertEquals(Decision.PERMIT, future.get());
				}
			}
		}finally{
			executor.shutdownNow();
		}
	}

	/** The cache keeps the most recently used patterns: the least recently used one is compiled again after eviction */
	@Test
	void theLeastRecentlyUsedPatternIsEvicted() throws Exception
	{
		Fixture fixture = new Fixture(new CompilesOnce(), 2);
		try(PolicyDecisionPoint dynamic = fixture.pdp("dynamic", fixture.dynamicPolicy())){
			assertEquals(Decision.PERMIT, decide(dynamic, subject("user-1", "^user-1$")).getDecision());
			assertEquals(Decision.PERMIT, decide(dynamic, subject("user-2", "^user-2$")).getDecision());
			// The third pattern evicts the first, the least recently used
			assertEquals(Decision.PERMIT, decide(dynamic, subject("user-3", "^user-3$")).getDecision());
			assertEquals(Decision.PERMIT, decide(dynamic, subject("user-2", "^user-2$")).getDecision());
			assertEquals(Decision.PERMIT, decide(dynamic, subject("user-3", "^user-3$")).getDecision());
			assertMalformed(decide(dynamic, subject("user-1", "^user-1$")));
		}
	}

	/** Three distinct patterns through the dynamic policy: more than a cache of two holds */
	private static void churnTheCache(PolicyDecisionPoint dynamic)
	{
		for(int i = 0; i < 3; i++){
			assertEquals(Decision.PERMIT, decide(dynamic, subject("user-" + i, "^user-" + i + "$")).getDecision(), "churn " + i);
		}
	}

	private static void assertMalformed(Result result)
	{
		Status status = result.getStatus();
		assertEquals(Decision.INDETERMINATE_P, result.getDecision(), () -> describe(status));
		assertEquals(StatusCodeId.SYNTAX_ERROR, status.getStatusCode().getValue(), () -> describe(status));
		assertEquals(Optional.of(RegExpFunctions.INVALID_PATTERN_MESSAGE), status.getMessage(), () -> describe(status));
	}

	private static void assertLimit(Result result)
	{
		Status status = result.getStatus();
		assertEquals(Decision.INDETERMINATE_P, result.getDecision(), () -> describe(status));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status.getStatusCode().getValue(), () -> describe(status));
		assertEquals(Optional.of(RegexBudget.LIMIT_EXCEEDED_MESSAGE), status.getMessage(), () -> describe(status));
	}

	/** The status with its error chain, for a failure message */
	private static String describe(Status status)
	{
		StringBuilder text = new StringBuilder(status.toString());
		for(Throwable error = status.getError().orElse(null); error != null; error = error.getCause()){
			text.append("\n  caused by ").append(error);
			if(error.getCause() == error){
				break;
			}
		}
		return text.toString();
	}

	private static Result decide(PolicyDecisionPoint pdp, RequestContext request)
	{
		Collection<Result> results = pdp.decide(request).getResults();
		assertEquals(1, results.size());
		return results.iterator().next();
	}

	private static RequestContext subject(String subjectId)
	{
		return RequestContext.builder()
				.attributes(Category.subjectAccess().entity(Entity.builder()
						.attribute(Attribute.builder(SUBJECT_ID).value(StringVal.of(subjectId)).build()).build()).build())
				.build();
	}

	private static RequestContext subject(String subjectId, String pattern)
	{
		return RequestContext.builder()
				.attributes(Category.subjectAccess().entity(Entity.builder()
						.attribute(Attribute.builder(SUBJECT_ID).value(StringVal.of(subjectId)).build())
						.attribute(Attribute.builder(PATTERN).value(StringVal.of(pattern)).build()).build()).build())
				.build();
	}

	/**
	 * One function provider over an engine double, with a cache of the given size, and the policies
	 * and PDPs over it.
	 */
	private static final class Fixture
	{
		private final FunctionProvider functions;
		private final PolicyRepository repository;

		Fixture(RegexEngine engine, int cacheSize){
			this.functions = FunctionProvider.builder()
					.withRegexEngine(engine, RegexProfile.LEGACY,
							RegexLimits.builder().cacheSize(cacheSize).build())
					.withDefaultFunctions()
					.build();
			this.repository = PolicyRepository.build("compile-once").withFunctionProvider(functions).build();
		}

		PolicyDecisionPoint pdp(String name, CompositeDecisionRule root){
			return PolicyDecisionPointBuilder.builder("compile-once-" + name)
					.policyRepository(repository)
					.rootPolicy(root)
					.pip(mock(PolicyInformationPoint.class))
					.build();
		}

		/** A Permit rule whose target matches the literal pattern against the subject id */
		Policy targetPolicy(String pattern){
			Target target = Target.builder().anyOf(MatchAnyOf.builder().anyOf(MatchAllOf.builder()
					.match(StringVal.of(pattern), designator(SUBJECT_ID), function(STRING_REGEXP_MATCH))
					.build()).build()).build();
			return policy("target", Rule.builder("literal-target", Effect.PERMIT).target(target));
		}

		/** A Permit rule whose condition matches the literal pattern against the subject id */
		Policy conditionPolicy(String pattern){
			Apply apply = Apply.builder(function(STRING_REGEXP_MATCH))
					.param(StringVal.of(pattern), oneAndOnly(SUBJECT_ID))
					.build();
			return policy("condition", Rule.builder("literal-condition", Effect.PERMIT).condition(Condition.condition(apply)));
		}

		/** A Permit rule whose condition matches the pattern of the request against the subject id */
		Policy dynamicPolicy(){
			Apply apply = Apply.builder(function(STRING_REGEXP_MATCH))
					.param(oneAndOnly(PATTERN), oneAndOnly(SUBJECT_ID))
					.build();
			return policy("dynamic", Rule.builder("dynamic-condition", Effect.PERMIT).condition(Condition.condition(apply)));
		}

		private Policy policy(String id, Rule.Builder rule){
			return Policy.builder(id).version("1.0")
					.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides"))
					.rule(rule)
					.build();
		}

		private Apply oneAndOnly(String attributeId){
			return Apply.builder(function(STRING_ONE_AND_ONLY)).param(designator(attributeId)).build();
		}

		private FunctionSpec function(String id){
			return functions.getFunction(id).orElseThrow();
		}

		private static AttributeDesignator designator(String attributeId){
			return AttributeDesignator.builder()
					.key(AttributeDesignatorKey.builder(attributeId).category(CategoryId.SUBJECT_ACCESS).dataType(XacmlTypes.STRING).build())
					.mustBePresent(true)
					.build();
		}
	}

	/** The jdk engine, which compiles a pattern once and rejects it as malformed after that */
	static final class CompilesOnce implements RegexEngine
	{
		private final RegexEngine jdk = new JdkRegexEngine();
		private final Set<String> compiled = ConcurrentHashMap.newKeySet();

		@Override
		public String name(){
			return "compiles-once";
		}

		@Override
		public Set<RegexProfile> profiles(){
			return jdk.profiles();
		}

		@Override
		public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
			if(!compiled.add(pattern)){
				throw RegexCompileException.malformed(COMPILED_AGAIN);
			}
			return jdk.compile(pattern, profile, limits);
		}
	}

	/** The jdk engine, whose first compilation of each pattern fails as given */
	static final class FailsFirst implements RegexEngine
	{
		private final RegexEngine jdk = new JdkRegexEngine();
		private final Supplier<RuntimeException> failure;
		private final Set<String> failed = ConcurrentHashMap.newKeySet();

		FailsFirst(Supplier<RuntimeException> failure){
			this.failure = failure;
		}

		@Override
		public String name(){
			return "fails-first";
		}

		@Override
		public Set<RegexProfile> profiles(){
			return jdk.profiles();
		}

		@Override
		public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
			if(failed.add(pattern)){
				throw failure.get();
			}
			return jdk.compile(pattern, profile, limits);
		}
	}
}
