package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.impl.AggregatingFunctionProvider;
import org.xacml4j.v30.types.XacmlTypes;

public class FunctionProviderDuplicateIdTest
{
	@Test
	public void duplicatePrimaryFunctionIdentifierFailsWithBothSources()
	{
		FunctionSpec first = spec("urn:test:function:duplicate", null, "source-one");
		FunctionSpec second = spec("urn:test:function:duplicate", null, "source-two");

		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> new AggregatingFunctionProvider(List.of(provider("provider-one", first),
						provider("provider-two", second))));

		assertTrue(failure.getMessage().contains("urn:test:function:duplicate"));
		assertTrue(failure.getMessage().contains("source-one"));
		assertTrue(failure.getMessage().contains("source-two"));
	}

	@Test
	public void legacyFunctionIdentifierCannotCollideWithAnotherPrimaryIdentifier()
	{
		String collisionId = "urn:test:function:colliding-alias";
		FunctionSpec legacyOwner = spec("urn:test:function:canonical", collisionId, "legacy-owner");
		FunctionSpec primaryOwner = spec(collisionId, null, "primary-owner");

		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> new AggregatingFunctionProvider(List.of(provider("legacy-provider", legacyOwner),
						provider("primary-provider", primaryOwner))));

		assertTrue(failure.getMessage().contains(collisionId));
		assertTrue(failure.getMessage().contains("legacy-owner"));
		assertTrue(failure.getMessage().contains("primary-owner"));
	}

	@Test
	public void addingSameProviderInstanceTwiceIsIdempotent()
	{
		FunctionSpec function = spec("urn:test:function:once", null, "same-provider-spec");
		FunctionProvider provider = provider("same-provider", function);
		FunctionProvider aggregate = new AggregatingFunctionProvider(Arrays.asList(provider, provider));

		assertSame(function, aggregate.getFunction(function.getId()).orElseThrow());
		assertEquals(1, aggregate.getProvidedFunctions().size());
	}

	@Test
	public void addingSameSpecObjectFromDifferentProvidersIsIdempotent()
	{
		FunctionSpec function = spec("urn:test:function:shared-spec", null, "shared-spec");
		FunctionProvider aggregate = new AggregatingFunctionProvider(List.of(
				provider("provider-a", function), provider("provider-b", function)));

		assertSame(function, aggregate.getFunction(function.getId()).orElseThrow());
		assertEquals(1, aggregate.getProvidedFunctions().size());
	}

	@Test
	public void defaultAndExplicitNonConflictingProviderCanBeCombined()
	{
		FunctionSpec explicit = spec("urn:test:function:explicit", null, "explicit-provider-spec");
		FunctionProvider aggregate = FunctionProviderBuilder.builder()
				.provider(provider("explicit-provider", explicit))
				.build();

		assertNotNull(aggregate.getFunction(explicit.getId()).orElse(null));
		assertNotNull(aggregate.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal").orElse(null));
	}

	@Test
	public void defaultAndDiscoveredProvidersCanBeCombined()
	{
		FunctionProvider aggregate = FunctionProviderBuilder.builder()
				.withDiscoveredFunctions()
				.withDefaultFunctions()
				.build();

		assertNotNull(aggregate.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal").orElse(null));
	}

	private static FunctionSpec spec(String id, String legacyId, String shortId)
	{
		return FunctionSpecBuilder.builder(id, legacyId)
				.shortId(shortId)
				.build(XacmlTypes.INTEGER, (spec, arguments) -> true, mock(FunctionInvocation.class));
	}

	private static FunctionProvider provider(String name, FunctionSpec... functions)
	{
		List<FunctionSpec> specs = List.of(functions);
		return new FunctionProvider()
		{
			@Override
			public Optional<FunctionSpec> getFunction(String functionId)
			{
				return specs.stream().filter(spec -> spec.getId().equals(functionId)).findFirst();
			}

			@Override
			public Collection<FunctionSpec> getProvidedFunctions()
			{
				return specs;
			}

			@Override
			public String getId()
			{
				return name;
			}
		};
	}
}
