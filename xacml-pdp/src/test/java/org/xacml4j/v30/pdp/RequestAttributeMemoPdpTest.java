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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Cycle A of change optimize-pdp-evaluation through a real PDP (review RVP): a request with two
 * resources is split into two individual decisions by the default handlers; each decision sees
 * its own resource-id bag, the same bag for every test within it, and none from the other.
 */
public class RequestAttributeMemoPdpTest
{
	private static final AttributeDesignatorKey RESOURCE_ID = AttributeDesignatorKey.builder()
			.category(CategoryId.RESOURCE).attributeId("urn:xacml4j:test:resource-id")
			.dataType(XacmlTypes.STRING).build();

	@Test
	public void eachIndividualDecisionResolvesItsOwnResource() throws Exception
	{
		List<BagOfValues> seen = Collections.synchronizedList(new ArrayList<>());
		CompositeDecisionRule root = Mockito.mock(CompositeDecisionRule.class);
		when(root.createContext(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(root.evaluate(Mockito.any())).thenAnswer(invocation -> {
			EvaluationContext context = invocation.getArgument(0);
			BagOfValues first = context.resolve(RESOURCE_ID).orElseThrow();
			BagOfValues again = context.resolve(RESOURCE_ID).orElseThrow();
			if(first != again){
				throw new AssertionError("two bags for one attribute in one decision");
			}
			seen.add(first);
			return Decision.PERMIT;
		});
		PolicyInformationPoint pip = Mockito.mock(PolicyInformationPoint.class);
		when(pip.resolve(Mockito.any(), Mockito.any(AttributeDesignatorKey.class))).thenReturn(Optional.empty());
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("memo-test")
				.policyRepository(Mockito.mock(PolicyRepository.class))
				.pip(pip)
				.rootPolicy(root)
				.defaultRequestHandlers()
				.build();

		ResponseContext response = pdp.decide(RequestContext.builder()
				.attributes(resource("doc-1"), resource("doc-2"))
				.build());

		assertThat(response.getResults()).hasSize(2);
		assertThat(seen).containsExactly(XacmlTypes.STRING.ofAny("doc-1").toBag(), XacmlTypes.STRING.ofAny("doc-2").toBag());
	}

	private static Category resource(String id)
	{
		return Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder()
						.attribute(Attribute.builder("urn:xacml4j:test:resource-id").value(XacmlTypes.STRING.ofAny(id)).build())
						.build())
				.build();
	}
}
