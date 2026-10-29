package org.xacml4j.v30.spi.pip;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The context of one resolver call resolves the context keys of its resolver when first asked, in
 * their declared order, keeps the present ones, asks again when all were absent (design D27), and
 * makes the key of the call from the kept values (cycle AL of change optimize-pdp-evaluation).
 */
public class ResolverContextTest
{
	private static final String RESOLVER = "urn:xacml4j:test:resolver";
	private static final AttributeDesignatorKey USER = key("urn:xacml4j:test:user");
	private static final AttributeDesignatorKey ROLE = key("urn:xacml4j:test:role");

	@Test
	public void oneContextKeyIsResolvedWhenAskedAndMakesTheKeyOfTheCall()
	{
		EvaluationContext context = Mockito.mock(EvaluationContext.class);
		when(context.resolve(USER)).thenReturn(Optional.of(bag("alice")));
		ResolverContext call = ResolverContext.createContext(context, resolver(USER));

		assertEquals(Optional.of(bag("alice")), call.resolveContextRef(USER));
		assertEquals(Map.of(USER, bag("alice")), call.getResolvedKeys());
		assertEquals(ResolverCacheKey.builder(RESOLVER).key(bag("alice")).build(), call.getCacheKey());
	}

	@Test
	public void contextKeysKeepTheirDeclaredOrderAndTheAbsentOnesAreLeftOut()
	{
		EvaluationContext oneAbsent = Mockito.mock(EvaluationContext.class);
		when(oneAbsent.resolve(USER)).thenReturn(Optional.empty());
		when(oneAbsent.resolve(ROLE)).thenReturn(Optional.of(bag("admin")));
		EvaluationContext bothPresent = Mockito.mock(EvaluationContext.class);
		when(bothPresent.resolve(USER)).thenReturn(Optional.of(bag("alice")));
		when(bothPresent.resolve(ROLE)).thenReturn(Optional.of(bag("admin")));

		ResolverContext call = ResolverContext.createContext(oneAbsent, resolver(USER, ROLE));
		assertEquals(Optional.empty(), call.resolveContextRef(USER));
		assertEquals(Optional.of(bag("admin")), call.resolveContextRef(ROLE));
		assertEquals(List.of(ROLE), List.copyOf(call.getResolvedKeys().keySet()));
		assertEquals(ResolverCacheKey.builder(RESOLVER).key(bag("admin")).build(), call.getCacheKey());
		assertEquals(ResolverCacheKey.builder(RESOLVER).key(bag("alice"), bag("admin")).build(),
				ResolverContext.createContext(bothPresent, resolver(USER, ROLE)).getCacheKey());
	}

	@Test
	public void contextKeysThatWereAllAbsentAreAskedAgain()
	{
		EvaluationContext context = Mockito.mock(EvaluationContext.class);
		when(context.resolve(USER)).thenReturn(Optional.empty(), Optional.of(bag("alice")));
		ResolverContext call = ResolverContext.createContext(context, resolver(USER));

		assertEquals(Optional.empty(), call.resolveContextRef(USER));
		assertEquals(Optional.of(bag("alice")), call.resolveContextRef(USER));
	}

	@Test
	public void aResolverWithoutContextKeysHasTheKeyOfItsIdAlone()
	{
		ResolverContext call = ResolverContext.createContext(Mockito.mock(EvaluationContext.class), resolver());

		assertEquals(ResolverCacheKey.builder(RESOLVER).build(), call.getCacheKey());
		assertEquals(Map.of(), call.getResolvedKeys());
	}

	/** A cacheable resolver of one string attribute with the given context keys */
	private static AttributeResolverDescriptor resolver(AttributeReferenceKey... contextKeys)
	{
		AttributeResolverDescriptor.Builder b = AttributeResolverDescriptor.builder(RESOLVER, "Test Resolver", CategoryId.SUBJECT_ACCESS)
				.cache(30).attribute("urn:xacml4j:test:attribute", XacmlTypes.STRING);
		for(AttributeReferenceKey contextKey : contextKeys){
			b.contextRef(contextKey);
		}
		return b.build(c -> Map.of());
	}

	private static AttributeDesignatorKey key(String attributeId)
	{
		return AttributeDesignatorKey.builder().category(CategoryId.SUBJECT_ACCESS)
				.attributeId(attributeId).dataType(XacmlTypes.STRING).build();
	}

	private static BagOfValues bag(String value)
	{
		return XacmlTypes.STRING.ofAny(value).toBag();
	}
}
