package org.xacml4j.v30.spi.pip;

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Clock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableMap;

/**
 * A resolver cache key is equal by the contents of its bags and hashes the same way
 * (bead policy4j-3huw, design D27 of optimize-pdp-evaluation).
 */
public class ResolverCacheKeyTest
{
	private static final String RESOLVER = "urn:xacml4j:test:resolver";

	private static final AttributeDesignatorKey USER = AttributeDesignatorKey.builder()
			.category(CategoryId.SUBJECT_ACCESS).attributeId("urn:xacml4j:test:user")
			.dataType(XacmlTypes.STRING).build();

	private static final AttributeDesignatorKey ROLE = AttributeDesignatorKey.builder()
			.category(CategoryId.SUBJECT_ACCESS).attributeId("urn:xacml4j:test:role")
			.dataType(XacmlTypes.STRING).build();

	private static ResolverCacheKey key(String resolver, String... values)
	{
		ResolverCacheKey.Builder b = ResolverCacheKey.builder(resolver);
		for(String value : values){
			b.key(XacmlTypes.STRING.ofAny(value).toBag());
		}
		return b.build();
	}

	@Test
	public void equalKeysBuiltApartFindEachOtherInAHashedCollection()
	{
		ResolverCacheKey first = key(RESOLVER, "alice", "admin");
		ResolverCacheKey second = key(RESOLVER, "alice", "admin");

		assertEquals(first, second);
		assertEquals(first.hashCode(), second.hashCode());
		Map<ResolverCacheKey, String> cache = new HashMap<>();
		cache.put(first, "set");
		assertEquals("set", cache.get(second));
		Set<ResolverCacheKey> keys = new HashSet<>();
		keys.add(first);
		assertTrue(keys.contains(second));
	}

	@Test
	public void keysThatDifferInResolverValuesOrOrderAreNotEqual()
	{
		ResolverCacheKey key = key(RESOLVER, "alice", "admin");

		assertNotEquals(key, key("urn:xacml4j:test:other", "alice", "admin"));
		assertNotEquals(key, key(RESOLVER, "alice", "user"));
		assertNotEquals(key, key(RESOLVER, "admin", "alice"));
		assertNotEquals(key, key(RESOLVER, "alice"));
	}

	/** The key made from resolved values, as the resolver context makes it, equals the key the builder makes of them (cycle AL) */
	@Test
	public void aKeyFromResolvedValuesEqualsTheBuiltKey()
	{
		ResolverCacheKey direct = ResolverCacheKey.of(RESOLVER, List.of(bag("alice"), bag("admin")));

		assertEquals(key(RESOLVER, "alice", "admin"), direct);
		assertEquals(key(RESOLVER, "alice", "admin").hashCode(), direct.hashCode());
		assertEquals(key(RESOLVER), ResolverCacheKey.of(RESOLVER, List.of()));
		assertNotEquals(direct, ResolverCacheKey.of(RESOLVER, List.of(bag("admin"), bag("alice"))));
	}

	private static BagOfValues bag(String value)
	{
		return XacmlTypes.STRING.ofAny(value).toBag();
	}

	@Test
	public void keySurvivesAStream() throws Exception
	{
		ResolverCacheKey key = key(RESOLVER, "alice", "admin");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try(ObjectOutputStream out = new ObjectOutputStream(bytes)){
			out.writeObject(key);
		}
		ResolverCacheKey loaded;
		try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){
			loaded = (ResolverCacheKey) in.readObject();
		}

		assertEquals(key, loaded);
		assertEquals(key.hashCode(), loaded.hashCode());
	}

	/** The stream was written by the class before the fix, with the hash it computed then. */
	@Test
	public void keyFromAStreamWrittenBeforeTheFixFindsItsEntry() throws Exception
	{
		ResolverCacheKey loaded;
		try(ObjectInputStream in = new ObjectInputStream(
				getClass().getResourceAsStream("resolver-cache-key-before-3huw.ser"))){
			loaded = (ResolverCacheKey) in.readObject();
		}
		ResolverCacheKey fresh = key(RESOLVER, "alice", "admin");

		assertEquals(fresh, loaded);
		assertEquals(fresh.hashCode(), loaded.hashCode());
		Map<ResolverCacheKey, String> cache = new HashMap<>();
		cache.put(loaded, "set");
		assertEquals("set", cache.get(fresh));
	}

	@Test
	public void twoDecisionsShareAPrimedCache()
	{
		AtomicInteger calls = new AtomicInteger();
		AttributeResolverDescriptor descriptor = AttributeResolverDescriptor
				.builder(RESOLVER, "Test Resolver", CategoryId.SUBJECT_ACCESS)
				.cache(30)
				.attribute(ROLE.getAttributeId(), XacmlTypes.STRING)
				.contextRef(USER)
				.build(c->ImmutableMap.of(ROLE.getAttributeId(),
						XacmlTypes.STRING.ofAny("role-" + calls.incrementAndGet()).toBag()));
		PolicyInformationPoint pip = PolicyInformationPoint.builder("test")
				.cacheProvider(new MapCache())
				.registry(ResolverRegistry.builder().withAttributeResolver(descriptor).build())
				.build();

		Optional<BagOfValues> first = pip.resolve(decision("alice"), ROLE);
		Optional<BagOfValues> second = pip.resolve(decision("alice"), ROLE);

		assertEquals(Optional.of(XacmlTypes.STRING.ofAny("role-1").toBag()), first);
		assertEquals(first, second);
	}

	private static EvaluationContext decision(String user)
	{
		EvaluationContext context = Mockito.mock(EvaluationContext.class);
		when(context.resolve(USER)).thenReturn(Optional.of(XacmlTypes.STRING.ofAny(user).toBag()));
		when(context.getClock()).thenReturn(Clock.systemUTC());
		return context;
	}

	/** A cache in a hashed map, as a provider would keep it */
	private static final class MapCache extends BasePolicyInformationPointCacheProvider
	{
		private final Map<ResolverCacheKey, AttributeSet> attributes = new HashMap<>();

		@Override
		protected ContentRef doGetContent(ResolverContext context) {
			return null;
		}

		@Override
		protected AttributeSet doGetAttributes(ResolverContext context) {
			return attributes.get(context.getCacheKey());
		}

		@Override
		protected void doPutContent(ResolverContext context, ContentRef content) {
		}

		@Override
		protected void doPutAttributes(ResolverContext context, AttributeSet v) {
			attributes.put(context.getCacheKey(), v);
		}
	}
}
