package org.xacml4j.jmh.corpus;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.LookupQuery;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpora;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.types.Value;

/**
 * Spec requirement "Synthetic corpus" (design D9). The PDP cross-check of
 * the generated decisions is in {@code PdpFixtureTest}.
 */
public class SyntheticCorpusTest
{
	private static final String DENY_OVERRIDES = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides";
	private static final Pattern RESOURCE = Pattern.compile("resource-\\d+");

	@Test
	public void sameCountGivesEqualPolicySourcesAndRequests() throws Exception
	{
		PolicyCorpus first = PolicyCorpora.load("synthetic:50");
		PolicyCorpus second = PolicyCorpora.load("synthetic:50");

		assertThat(second.policies()).isEqualTo(first.policies());
		assertThat(second.root()).isEqualTo(first.root());
		assertThat(second.requests()).isEqualTo(first.requests());
	}

	@ParameterizedTest(name = "synthetic:{0} has {1} requests")
	@CsvSource({"1, 3", "2, 5", "100, 7"})
	public void eachDistinctSlotHasAPermitAndADenyRequestPlusOneNotApplicableRequest(int count, int requests)
			throws Exception
	{
		PolicyCorpus corpus = PolicyCorpora.load("synthetic:" + count);

		List<String> ids = ids(corpus);
		assertThat(ids).hasSize(requests);
		assertThat(ids).containsNoDuplicates();
		assertThat(ids).containsAtLeast("first-permit", "first-deny", "unknown-notapplicable");
		assertThat(decisions(corpus)).containsAtLeast(Decision.PERMIT, Decision.DENY, Decision.NOT_APPLICABLE);
		for(CorpusRequest request : corpus.requests()){
			Optional<Decision> named = outcomeNamedBy(request.id());
			assertWithMessage("outcome named by request id %s", request.id()).that(named.isPresent()).isTrue();
			assertWithMessage("expected decision of %s", request.id()).that(request.expected()).isEqualTo(named.get());
		}
	}

	@Test
	public void requestsOfSynthetic100TargetPolicies0And50And99() throws Exception
	{
		PolicyCorpus corpus = PolicyCorpora.load("synthetic:100");

		assertThat(ids(corpus)).containsExactly("first-permit", "first-deny", "middle-permit", "middle-deny",
				"last-permit", "last-deny", "unknown-notapplicable");
		for(CorpusRequest request : corpus.requests()){
			if(request.id().startsWith("unknown-")){
				continue;
			}
			int slot = request.id().startsWith("first-") ? 0 : request.id().startsWith("middle-") ? 50 : 99;
			assertWithMessage("resource of %s", request.id())
					.that(resources(request)).containsExactly("resource-" + slot);
		}
	}

	@Test
	public void corpusHoldsCountPoliciesAndARootThatRefersToEachOfThem() throws Exception
	{
		PolicyCorpus corpus = PolicyCorpora.load("synthetic:10");

		assertThat(corpus.policies()).hasSize(11);
		for(PolicySource source : corpus.policies()){
			assertWithMessage("media type of %s", source.name())
					.that(source.mediaType()).isEqualTo(MediaType.Type.XACML30_XML);
		}
		List<CompositeDecisionRule> rules = PdpFixture.parse(corpus.policies());
		CompositeDecisionRule root = rules.get(corpus.rootIndex());
		assertThat(root).isInstanceOf(PolicySet.class);
		assertThat(((PolicySet)root).getPolicyDecisionCombiningAlgorithm().getId()).isEqualTo(DENY_OVERRIDES);
		List<CompositeDecisionRule> policies = new ArrayList<>(rules);
		policies.remove(root);
		for(CompositeDecisionRule policy : policies){
			assertWithMessage("rule %s", policy.getId()).that(policy).isInstanceOf(Policy.class);
		}
		List<CompositeDecisionRule> referenced = new ArrayList<>();
		for(LookupQuery query : LookupQuery.of(rules)){
			assertThat(query.kind()).isNotEqualTo(LookupQuery.Kind.POLICY_SET_REFERENCE);
			if(query.kind() == LookupQuery.Kind.POLICY_REFERENCE){
				referenced.add(query.expected());
			}
		}
		assertThat(referenced).containsExactlyElementsIn(policies);
	}

	@Test
	public void countDefaultsTo100() throws Exception
	{
		assertThat(PolicyCorpora.load("synthetic").policies()).hasSize(101);
	}

	@ParameterizedTest(name = "synthetic:{0}")
	@ValueSource(strings = {"0", "100001", "-3", "ten"})
	public void countThatIsNotAnIntegerFrom1To100000IsRejected(String count)
	{
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpora.load("synthetic:" + count));
	}

	/** @return the decision that a request id names: {@code <slot>-permit}, {@code <slot>-deny}, or {@code unknown-notapplicable} */
	private static Optional<Decision> outcomeNamedBy(String id)
	{
		if(id.endsWith("-permit")){
			return Optional.of(Decision.PERMIT);
		}
		if(id.endsWith("-deny")){
			return Optional.of(Decision.DENY);
		}
		if(id.endsWith("-notapplicable")){
			return Optional.of(Decision.NOT_APPLICABLE);
		}
		return Optional.empty();
	}

	/** @return every attribute value of the request of the form {@code resource-<i>} */
	private static List<String> resources(CorpusRequest request)
	{
		List<String> found = new ArrayList<>();
		for(Category category : request.request().getCategories()){
			for(Attribute attribute : category.getEntity().attributes()){
				for(Value<?> value : attribute.values()){
					String text = String.valueOf(value.get());
					if(RESOURCE.matcher(text).matches()){
						found.add(text);
					}
				}
			}
		}
		return found;
	}

	private static List<String> ids(PolicyCorpus corpus)
	{
		List<String> ids = new ArrayList<>();
		for(CorpusRequest request : corpus.requests()){
			ids.add(request.id());
		}
		return ids;
	}

	private static List<Decision> decisions(PolicyCorpus corpus)
	{
		List<Decision> decisions = new ArrayList<>();
		for(CorpusRequest request : corpus.requests()){
			decisions.add(request.expected());
		}
		return decisions;
	}
}
