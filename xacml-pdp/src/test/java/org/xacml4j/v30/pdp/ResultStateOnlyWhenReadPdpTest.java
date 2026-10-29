package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J PDP
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
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Cycle Y of change optimize-pdp-evaluation (design D32) through a real decision point: what a
 * result lists does not depend on whether the state behind it was created.
 */
public class ResultStateOnlyWhenReadPdpTest
{
	private static final CompositeDecisionRuleIDReference REF_A = PolicyIDReference.builder("urn:xacml4j:test:a").build();
	private static final CompositeDecisionRuleIDReference REF_B = PolicyIDReference.builder("urn:xacml4j:test:b").build();

	private static CompositeDecisionRule policy(String id, CompositeDecisionRuleIDReference reference)
	{
		CompositeDecisionRule policy = Mockito.mock(CompositeDecisionRule.class);
		when(policy.getId()).thenReturn(id);
		when(policy.getReference()).thenReturn(reference);
		return policy;
	}

	/** A root whose evaluation finds two policies applicable, b before a, and adds a response */
	private static PolicyDecisionPoint pdp()
	{
		CompositeDecisionRule a = policy("urn:xacml4j:test:a", REF_A);
		CompositeDecisionRule b = policy("urn:xacml4j:test:b", REF_B);
		return pdp(context -> {
			context.addEvaluationResult(b, Decision.PERMIT);
			context.addEvaluationResult(a, Decision.PERMIT);
			context.addObligations(Decision.PERMIT, List.of(
					Obligation.builder("urn:xacml4j:test:obligation-2", Effect.PERMIT).build(),
					Obligation.builder("urn:xacml4j:test:obligation-1", Effect.PERMIT).build()));
			context.addAdvices(Decision.PERMIT, List.of(Advice.builder("urn:xacml4j:test:advice", Effect.PERMIT).build()));
		});
	}

	/** A root whose evaluation writes to the context of the decision and is Permit */
	private static PolicyDecisionPoint pdp(Consumer<EvaluationContext> evaluation)
	{
		CompositeDecisionRule root = Mockito.mock(CompositeDecisionRule.class);
		when(root.createContext(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(root.evaluate(Mockito.any())).thenAnswer(invocation -> {
			evaluation.accept(invocation.getArgument(0));
			return Decision.PERMIT;
		});
		return PolicyDecisionPointBuilder.builder("state-when-read-test")
				.policyRepository(Mockito.mock(PolicyRepository.class))
				.pip(Mockito.mock(PolicyInformationPoint.class))
				.defaultRequestHandlers()
				.rootPolicy(root)
				.build();
	}

	private static Attribute attribute(String id, String value, boolean includeInResult)
	{
		return Attribute.builder(id).includeInResult(includeInResult).value(XacmlTypes.STRING.ofAny(value)).build();
	}

	private static Category category(CategoryId id, Attribute... attributes)
	{
		return Category.builder(id).entity(Entity.builder().attribute(attributes).build()).build();
	}

	private static RequestContext request(boolean returnPolicyIdList)
	{
		return RequestContext.builder()
				.returnPolicyIdList(returnPolicyIdList)
				.attributes(category(CategoryId.SUBJECT_ACCESS, attribute("urn:xacml4j:test:subject-id", "alice", false)))
				.build();
	}

	private static Result decide(PolicyDecisionPoint pdp, RequestContext request)
	{
		return pdp.decide(request).getResults().iterator().next();
	}

	@Test
	public void policyIdentifiersAreListedInIdOrderOnlyWhenTheRequestAsksForThem()
	{
		PolicyDecisionPoint pdp = pdp();

		Result asked = decide(pdp, request(true));
		Result notAsked = decide(pdp, request(false));

		assertThat(asked.getDecision()).isEqualTo(Decision.PERMIT);
		assertThat(asked.getPolicyIdentifiers()).containsExactly(REF_A, REF_B).inOrder();
		assertThat(notAsked.getDecision()).isEqualTo(Decision.PERMIT);
		assertThat(notAsked.getPolicyIdentifiers()).isEmpty();
	}

	@Test
	public void obligationsAndAdviceOfTheResultAreUnchanged()
	{
		Result result = decide(pdp(), request(false));

		// The content; the order is the subject of obligationsKeepTheOrderOfTheFirstWriteOfTheirId
		assertThat(result.getObligations().stream().map(Obligation::getId).toList())
				.containsExactly("urn:xacml4j:test:obligation-2", "urn:xacml4j:test:obligation-1");
		assertThat(result.getAssociatedAdvice().stream().map(Advice::getId).toList())
				.containsExactly("urn:xacml4j:test:advice");
	}

	/** Cycle AU: the obligations of a result keep the order of the first write of each id; a later one merges in its place. */
	@Test
	public void obligationsKeepTheOrderOfTheFirstWriteOfTheirId()
	{
		Obligation c = Obligation.builder("c", Effect.PERMIT).build();
		Obligation a = Obligation.builder("a", Effect.PERMIT).attribute("o", XacmlTypes.STRING.ofAny("1")).build();
		Obligation b = Obligation.builder("b", Effect.PERMIT).build();
		Obligation laterA = Obligation.builder("a", Effect.PERMIT).attribute("o", XacmlTypes.STRING.ofAny("2")).build();
		PolicyDecisionPoint pdp = pdp(context -> {
			context.addObligations(Decision.PERMIT, List.of(c, a));
			context.addObligations(Decision.PERMIT, List.of(b, laterA));
		});

		Result result = decide(pdp, request(false));

		assertThat(result.getObligations()).containsExactly(c, a.merge(laterA), b).inOrder();
		assertThat(result.getObligation("a")).isEqualTo(a.merge(laterA));
	}

	@Test
	public void includeInResultAttributesKeepTheirCategoriesAttributesAndOrder()
	{
		RequestContext request = RequestContext.builder().attributes(
				category(CategoryId.SUBJECT_ACCESS,
						attribute("urn:xacml4j:test:subject-id", "alice", true),
						attribute("urn:xacml4j:test:role", "admin", false)),
				category(CategoryId.RESOURCE, attribute("urn:xacml4j:test:resource-id", "doc-1", false)),
				category(CategoryId.ACTION, attribute("urn:xacml4j:test:action-id", "read", true)))
				.build();

		Result result = decide(pdp(), request);

		assertThat(result.getIncludeInResultAttributes()).hasSize(2);
		Iterator<Category> included = result.getIncludeInResultAttributes().iterator();
		Category subject = included.next();
		Category action = included.next();
		assertThat(subject.getCategoryId()).isEqualTo(CategoryId.SUBJECT_ACCESS);
		assertThat(subject.getEntity().attributes()).containsExactly(attribute("urn:xacml4j:test:subject-id", "alice", true));
		assertThat(action.getCategoryId()).isEqualTo(CategoryId.ACTION);
		assertThat(action.getEntity().attributes()).containsExactly(attribute("urn:xacml4j:test:action-id", "read", true));
		assertThat(decide(pdp(), request(false)).getIncludeInResultAttributes()).isEmpty();
	}

	/** Cycle AM: the individual requests of a multiple decision request share a category and what it includes. */
	@Test
	public void individualRequestsIncludeTheSharedAndTheirOwnCategories()
	{
		Category subject = category(CategoryId.SUBJECT_ACCESS, attribute("urn:xacml4j:test:subject-id", "alice", true));
		Category first = category(CategoryId.RESOURCE, attribute("urn:xacml4j:test:resource-id", "doc-1", true),
				attribute("urn:xacml4j:test:owner", "bob", false));
		Category second = category(CategoryId.RESOURCE, attribute("urn:xacml4j:test:resource-id", "doc-2", true));
		RequestContext request = RequestContext.builder().attributes(subject, first, second).build();

		List<Result> results = List.copyOf(pdp().decide(request).getResults());

		assertThat(results).hasSize(2);
		assertThat(results.get(0).getIncludeInResultAttributes()).containsExactly(subject,
				category(CategoryId.RESOURCE, attribute("urn:xacml4j:test:resource-id", "doc-1", true))).inOrder();
		assertThat(results.get(1).getIncludeInResultAttributes()).containsExactly(subject, second).inOrder();
	}
}
