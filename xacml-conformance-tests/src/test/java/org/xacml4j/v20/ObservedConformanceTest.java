package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Conformance Tests
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.ConditionResult;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.spi.observe.AttributeSource;
import org.xacml4j.v30.spi.observe.DecisionObserver;

/**
 * Every case of the conformance suites decided once more with a recording observer: the response is
 * the one that the decision point gives without an observer, and the observer sees the callbacks of
 * the contract, each with the innermost open token and every span ended (change
 * optimize-pdp-evaluation, design D34, task 36.3)
 */
public class ObservedConformanceTest
{
	private static final Set<String> CLOCK = Set.of(
			"urn:oasis:names:tc:xacml:1.0:environment:current-time",
			"urn:oasis:names:tc:xacml:1.0:environment:current-date",
			"urn:oasis:names:tc:xacml:1.0:environment:current-dateTime");

	static Stream<ConformanceManifest.Case> cases() throws Exception
	{
		return Stream.of(ConformanceManifest.cases("oasis20"), ConformanceManifest.cases("oasis30"),
				ConformanceManifest.cases("rsa2008"), ConformanceManifest.cases("example")).flatMap(s -> s);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("cases")
	public void aRecordingObserverLeavesTheResponseUnchanged(ConformanceManifest.Case testCase) throws Exception
	{
		ConformanceCaseRunner.requireEnabled(testCase);
		PolicyDecisionPoint plain;
		try{
			plain = ConformanceCaseRunner.buildPdp(testCase);
		}catch(AssertionError rejected){
			if(ConformanceCaseRunner.expectedStaticRejection(testCase, rejected.getCause())){
				Assumptions.abort("The policy is rejected when it is loaded, so there is no decision to observe");
			}
			throw rejected;
		}
		Optional<RequestContext> request = request(testCase);
		Assumptions.assumeTrue(request.isPresent(), "The request is rejected when it is parsed, so there is no decision");
		Recorder recorder = new Recorder();
		PolicyDecisionPoint observed = ConformanceCaseRunner.buildPdp(testCase, recorder);

		assertEquals(withoutClockValues(plain.decide(request.get())), withoutClockValues(observed.decide(request.get())));
		assertEquals(List.of(), recorder.violations);
		assertEquals(1, recorder.requests);
		assertEquals(recorder.decisions, recorder.terminals);
		assertTrue(recorder.decisions == 0 || recorder.evaluations > 0, "An observed decision evaluates its root policy");
	}

	/**
	 * The results, with every part compared, except the values of the environment's current time, date,
	 * and date-time: the information point reads them from the clock at each decision
	 */
	private static List<List<Object>> withoutClockValues(ResponseContext response)
	{
		List<List<Object>> results = new ArrayList<>();
		for(Result r : response.getResults()){
			Set<List<Object>> resolved = new HashSet<>();
			for(Category category : r.getResolvedAttributes()){
				for(Attribute attribute : category.getEntity().attributes()){
					resolved.add(CLOCK.contains(attribute.getAttributeId())
							?List.of(category.getCategoryId(), attribute.getAttributeId())
							:List.of(category.getCategoryId(), attribute));
				}
			}
			results.add(List.of(r.getDecision(), r.getStatus(), new ArrayList<>(r.getObligations()),
					new ArrayList<>(r.getAssociatedAdvice()), new ArrayList<>(r.getPolicyIdentifiers()),
					new HashSet<>(r.getIncludeInResultAttributes()), resolved));
		}
		return results;
	}

	private static Optional<RequestContext> request(ConformanceManifest.Case testCase) throws Exception
	{
		try{
			return Optional.of("oasis30".equals(testCase.suite())
					? Xacml30TestUtility.getRequest(testCase.requestFile())
					: Xacml20TestUtility.getRequest(testCase.requestFile()));
		}catch(SyntaxException invalidRequest){
			if(ConformanceCaseRunner.expectedRequestSyntaxError(testCase, invalidRequest)){
				return Optional.empty();
			}
			throw invalidRequest;
		}
	}

	/**
	 * Keeps the open spans on a stack and records each callback that does not get the innermost open
	 * token; it never throws, so the decision is the one an observer that does nothing gets
	 */
	private static final class Recorder implements DecisionObserver<Object>
	{
		private final Deque<Object> open = new ArrayDeque<>();
		private final List<String> violations = new ArrayList<>();
		private int requests;
		private int decisions;
		private int terminals;
		private int evaluations;

		private Object start(Object parent, String callback){
			expectInnermost(parent, callback);
			Object token = new Object();
			open.push(token);
			return token;
		}

		private void end(Object token, String callback){
			expectInnermost(token, callback);
			if(open.peek() == token){
				open.pop();
			}
		}

		private void expectInnermost(Object token, String callback){
			if(open.peek() != token){
				violations.add(callback + " did not get the innermost open token");
			}
		}

		@Override
		public Object requestStarted(PolicyDecisionPoint pdp, RequestContext request){
			requests++;
			if(!open.isEmpty()){
				violations.add("requestStarted with open spans");
			}
			Object token = new Object();
			open.push(token);
			return token;
		}

		@Override
		public void requestFinished(Object request, ResponseContext response){
			end(request, "requestFinished");
			if(!open.isEmpty()){
				violations.add("requestFinished with open spans");
			}
		}

		@Override
		public void requestFailed(Object request, Throwable failure){
			end(request, "requestFailed");
		}

		@Override
		public Object decisionStarted(Object request, RequestContext individualRequest, long correlation, int number){
			decisions++;
			if(number != decisions){
				violations.add("decision " + number + " is not decision " + decisions + " of the call");
			}
			return start(request, "decisionStarted");
		}

		@Override
		public void decisionCacheHit(Object decision, Result result){
			expectInnermost(decision, "decisionCacheHit");
		}

		@Override
		public void decisionFinished(Object decision, Result result){
			terminals++;
			end(decision, "decisionFinished");
		}

		@Override
		public void decisionFailed(Object decision, Throwable failure){
			terminals++;
			end(decision, "decisionFailed");
		}

		@Override
		public Object evaluationStarted(Object parent, EvaluationContext context, DecisionRule rule){
			evaluations++;
			return start(parent, "evaluationStarted");
		}

		@Override
		public void evaluationFinished(Object evaluation, EvaluationContext context, DecisionRule rule, Decision decision){
			end(evaluation, "evaluationFinished");
		}

		@Override
		public void evaluationFailed(Object evaluation, EvaluationContext context, DecisionRule rule, Throwable failure){
			end(evaluation, "evaluationFailed");
		}

		@Override
		public Object extendedIndeterminateStarted(Object evaluation, EvaluationContext context, CompositeDecisionRule rule){
			return start(evaluation, "extendedIndeterminateStarted");
		}

		@Override
		public void extendedIndeterminateFinished(Object pass, EvaluationContext context, CompositeDecisionRule rule,
				Decision decision){
			end(pass, "extendedIndeterminateFinished");
		}

		@Override
		public void extendedIndeterminateFailed(Object pass, EvaluationContext context, CompositeDecisionRule rule,
				Throwable failure){
			end(pass, "extendedIndeterminateFailed");
		}

		@Override
		public Object attributeRequested(Object parent, EvaluationContext context, AttributeReferenceKey key){
			return start(parent, "attributeRequested");
		}

		@Override
		public void attributeResolved(Object attribute, EvaluationContext context, AttributeReferenceKey key,
				AttributeSource source, Optional<BagOfValues> value){
			end(attribute, "attributeResolved");
		}

		@Override
		public void attributeFailed(Object attribute, EvaluationContext context, AttributeReferenceKey key, Throwable failure){
			end(attribute, "attributeFailed");
		}

		@Override
		public Object resolverStarted(Object parent, EvaluationContext context, String resolverId, AttributeReferenceKey key){
			return start(parent, "resolverStarted");
		}

		@Override
		public void resolverCacheHit(Object resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key){
			expectInnermost(resolver, "resolverCacheHit");
		}

		@Override
		public void resolverFinished(Object resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
				Optional<BagOfValues> value){
			end(resolver, "resolverFinished");
		}

		@Override
		public void resolverFailed(Object resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
				Throwable failure){
			end(resolver, "resolverFailed");
		}

		@Override
		public void targetMatched(Object current, EvaluationContext context, DecisionRule rule, MatchResult result){
			expectInnermost(current, "targetMatched");
		}

		@Override
		public void conditionEvaluated(Object current, EvaluationContext context, DecisionRule rule, ConditionResult result){
			expectInnermost(current, "conditionEvaluated");
		}

		@Override
		public void childrenSelected(Object current, EvaluationContext context, CompositeDecisionRule rule,
				List<? extends DecisionRule> selected){
			expectInnermost(current, "childrenSelected");
		}

		@Override
		public void functionFailed(Object current, EvaluationContext context, FunctionSpec function, Throwable failure){
			expectInnermost(current, "functionFailed");
		}

		@Override
		public void responsesStaged(Object current, EvaluationContext context, DecisionRule rule, Decision decision,
				List<Advice> advice, List<Obligation> obligations){
			expectInnermost(current, "responsesStaged");
		}
	}
}
