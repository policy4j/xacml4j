package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Policy Decision Point
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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Cycle E of change optimize-pdp-evaluation through a real PDP (design D9, review RVP): the
 * resolved attributes of a result list only what the information point supplied, and
 * decisions on several threads do not see each other's state.
 */
public class DecisionFixedCostPdpTest
{
	private static final AttributeDesignatorKey RESOURCE_ID = AttributeDesignatorKey.builder()
			.category(CategoryId.RESOURCE).attributeId("urn:xacml4j:test:resource-id")
			.dataType(XacmlTypes.STRING).build();
	private static final AttributeDesignatorKey CLEARANCE = AttributeDesignatorKey.builder()
			.category(CategoryId.SUBJECT_ACCESS).attributeId("urn:xacml4j:test:clearance")
			.dataType(XacmlTypes.STRING).build();

	private static PolicyDecisionPoint pdp(CompositeDecisionRule root, PolicyInformationPoint pip)
	{
		return PolicyDecisionPointBuilder.builder("fixed-cost-test")
				.policyRepository(Mockito.mock(PolicyRepository.class))
				.pip(pip)
				.rootPolicy(root)
				.build();
	}

	private static RequestContext request(String resourceId)
	{
		return RequestContext.builder()
				.attributes(Category.builder(CategoryId.RESOURCE)
						.entity(Entity.builder()
								.attribute(Attribute.builder("urn:xacml4j:test:resource-id").value(XacmlTypes.STRING.ofAny(resourceId)).build())
								.build())
						.build())
				.build();
	}

	@Test
	public void resolvedAttributesListOnlyInformationPointBags() throws Exception
	{
		CompositeDecisionRule root = Mockito.mock(CompositeDecisionRule.class);
		when(root.createContext(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(root.evaluate(Mockito.any())).thenAnswer(invocation -> {
			EvaluationContext context = invocation.getArgument(0);
			String id = context.resolve(RESOURCE_ID).orElseThrow().value().get().toString();
			if(id.equals("with-pip")){
				context.resolve(CLEARANCE);
			}
			return Decision.PERMIT;
		});
		PolicyInformationPoint pip = Mockito.mock(PolicyInformationPoint.class);
		BagOfValues high = XacmlTypes.STRING.ofAny("high").toBag();
		when(pip.resolve(Mockito.any(), Mockito.any(AttributeDesignatorKey.class))).thenAnswer(invocation ->
				CLEARANCE.equals(invocation.getArgument(1)) ? Optional.of(high) : Optional.empty());
		PolicyDecisionPoint pdp = pdp(root, pip);

		Result without = pdp.decide(request("without-pip")).getResults().iterator().next();
		Result with = pdp.decide(request("with-pip")).getResults().iterator().next();

		assertThat(without.getResolvedAttributes()).isEmpty();
		assertThat(with.getResolvedAttributes()).hasSize(1);
		Category resolved = with.getResolvedAttributes().iterator().next();
		assertThat(resolved.getCategoryId()).isEqualTo(CategoryId.SUBJECT_ACCESS);
		assertThat(resolved.getEntity().attributes()).containsExactly(
				Attribute.builder("urn:xacml4j:test:clearance").value(XacmlTypes.STRING.ofAny("high")).build());
	}

	@Test
	public void resolvedAttributesKeepGroupedDuplicatesAndFloatingPointValues() throws Exception
	{
		AttributeDesignatorKey key = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS).attributeId("urn:xacml4j:test:measurements")
				.issuer("measurements").dataType(XacmlTypes.DOUBLE).build();
		BagOfValues values = XacmlTypes.DOUBLE.bagBuilder()
				.value(2.0, -0.0, Double.NaN, 0.0, 2.0, -0.0, Double.NaN).build();
		CompositeDecisionRule root = Mockito.mock(CompositeDecisionRule.class);
		when(root.createContext(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(root.evaluate(Mockito.any())).thenAnswer(invocation -> {
			EvaluationContext context = invocation.getArgument(0);
			context.resolve(key);
			return Decision.PERMIT;
		});
		PolicyInformationPoint pip = Mockito.mock(PolicyInformationPoint.class);
		when(pip.resolve(Mockito.any(), Mockito.eq(key))).thenReturn(Optional.of(values));

		Result result = pdp(root, pip).decide(RequestContext.builder().build()).getResults().iterator().next();

		assertThat(result.getDecision()).isEqualTo(Decision.PERMIT);
		assertThat(result.getResolvedAttributes()).hasSize(1);
		Category resolved = result.getResolvedAttributes().iterator().next();
		assertThat(resolved.getCategoryId()).isEqualTo(CategoryId.SUBJECT_ACCESS);
		assertThat(resolved.getEntity().attributes()).hasSize(1);
		Attribute attribute = resolved.getEntity().attributes().iterator().next();
		assertThat(attribute.getAttributeId()).isEqualTo(key.getAttributeId());
		assertThat(attribute.getIssuer()).isEqualTo("measurements");
		assertThat(attribute.values().stream().map(v -> v.get()).toList())
				.containsExactly(2.0, 2.0, -0.0, -0.0, Double.NaN, Double.NaN, 0.0).inOrder();
	}

	@Test
	public void decisionsOnSeveralThreadsKeepTheirOwnState() throws Exception
	{
		CompositeDecisionRule root = Mockito.mock(CompositeDecisionRule.class);
		when(root.createContext(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(root.evaluate(Mockito.any())).thenAnswer(invocation -> {
			EvaluationContext context = invocation.getArgument(0);
			String id = context.resolve(RESOURCE_ID).orElseThrow().value().get().toString();
			return Integer.parseInt(id.substring(id.lastIndexOf('-') + 1)) % 2 == 0 ? Decision.PERMIT : Decision.DENY;
		});
		PolicyInformationPoint pip = Mockito.mock(PolicyInformationPoint.class);
		when(pip.resolve(Mockito.any(), Mockito.any(AttributeDesignatorKey.class))).thenReturn(Optional.empty());
		PolicyDecisionPoint pdp = pdp(root, pip);
		int threads = 4;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try{
			CyclicBarrier start = new CyclicBarrier(threads);
			List<Future<Integer>> wrong = new ArrayList<>();
			for(int t = 0; t < threads; t++){
				int thread = t;
				wrong.add(pool.submit(() -> {
					start.await(10, TimeUnit.SECONDS);
					int mismatches = 0;
					for(int i = 0; i < 250; i++){
						int n = thread * 1000 + i;
						Decision expected = n % 2 == 0 ? Decision.PERMIT : Decision.DENY;
						Result r = pdp.decide(request("doc-" + n)).getResults().iterator().next();
						if(r.getDecision() != expected){
							mismatches++;
						}
					}
					return mismatches;
				}));
			}
			for(Future<Integer> f : wrong){
				assertThat(f.get(60, TimeUnit.SECONDS)).isEqualTo(0);
			}
		}finally{
			pool.shutdownNow();
		}
	}
}
