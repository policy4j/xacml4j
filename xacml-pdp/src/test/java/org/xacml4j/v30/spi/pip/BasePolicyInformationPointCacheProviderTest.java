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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The admission of a cache provider built on {@link BasePolicyInformationPointCacheProvider}: a
 * value of a cacheable resolver is kept while it is within the time to live of its resolver,
 * for attribute sets and for content alike (review R-FABLE-sol).
 */
public class BasePolicyInformationPointCacheProviderTest
{
	private static final String ROLE = "urn:xacml4j:test:role";
	private static final Instant NOW = Instant.parse("2026-10-28T10:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
	private static final Clock BEFORE_THE_TIME_TO_LIVE = Clock.fixed(NOW.minusSeconds(61), ZoneOffset.UTC);

	private final MapCache cache = new MapCache();
	private final AttributeResolverDescriptor profile = AttributeResolverDescriptor
			.builder("profile", "profile", CategoryId.SUBJECT_ACCESS).cache(60)
			.attribute(ROLE, XacmlTypes.STRING).build(context -> Map.of());
	private final ContentResolverDescriptor document = ContentResolverDescriptor
			.builder("document", "document", CategoryId.RESOURCE).cache(60).build(context -> null);
	private final Content content = Content.fromString("<record/>", Content.Type.XML_UTF8).orElseThrow();
	private EvaluationContext context;

	@BeforeEach
	public void init()
	{
		context = Mockito.mock(EvaluationContext.class);
		when(context.getClock()).thenReturn(CLOCK);
	}

	@Test
	public void freshAttributesAreKept()
	{
		ResolverContext call = ResolverContext.createContext(context, profile);
		AttributeSet fresh = attributes(CLOCK);

		cache.putAttributes(call, fresh);

		assertEquals(Optional.of(fresh), cache.getAttributes(call));
	}

	@Test
	public void attributesOlderThanTheTimeToLiveAreNotKept()
	{
		ResolverContext call = ResolverContext.createContext(context, profile);

		cache.putAttributes(call, attributes(BEFORE_THE_TIME_TO_LIVE));

		assertEquals(Optional.empty(), cache.getAttributes(call));
	}

	@Test
	public void freshContentIsKept()
	{
		ResolverContext call = ResolverContext.createContext(context, document);
		ContentRef fresh = content(CLOCK);

		cache.putContent(call, fresh);

		assertEquals(Optional.of(fresh), cache.getContent(call));
	}

	@Test
	public void contentOlderThanTheTimeToLiveIsNotKept()
	{
		ResolverContext call = ResolverContext.createContext(context, document);

		cache.putContent(call, content(BEFORE_THE_TIME_TO_LIVE));

		assertEquals(Optional.empty(), cache.getContent(call));
	}

	@Test
	public void valuesOfAResolverWithoutACacheAreNotKept()
	{
		AttributeResolverDescriptor uncached = AttributeResolverDescriptor
				.builder("profile", "profile", CategoryId.SUBJECT_ACCESS).noCache()
				.attribute(ROLE, XacmlTypes.STRING).build(c -> Map.of());
		ContentResolverDescriptor uncachedDocument = ContentResolverDescriptor
				.builder("document", "document", CategoryId.RESOURCE).noCache().build(c -> null);

		cache.putAttributes(ResolverContext.createContext(context, uncached),
				AttributeSet.builder(uncached).clock(CLOCK).attribute(ROLE, XacmlTypes.STRING.ofAny("admin").toBag()).build());
		cache.putContent(ResolverContext.createContext(context, uncachedDocument),
				ContentRef.builder().resolver(uncachedDocument).content(content).clock(CLOCK).build());

		assertTrue(cache.isEmpty());
	}

	private AttributeSet attributes(Clock createdAt)
	{
		return AttributeSet.builder(profile).clock(createdAt).attribute(ROLE, XacmlTypes.STRING.ofAny("admin").toBag()).build();
	}

	private ContentRef content(Clock createdAt)
	{
		return ContentRef.builder().resolver(document).content(content).clock(createdAt).build();
	}

	/** A provider that keeps what it is given in maps */
	private static final class MapCache extends BasePolicyInformationPointCacheProvider
	{
		private final Map<ResolverCacheKey, AttributeSet> attributes = new HashMap<>();
		private final Map<ResolverCacheKey, ContentRef> content = new HashMap<>();

		boolean isEmpty()
		{
			return attributes.isEmpty() && content.isEmpty();
		}

		@Override
		protected ContentRef doGetContent(ResolverContext context)
		{
			return content.get(context.getCacheKey());
		}

		@Override
		protected AttributeSet doGetAttributes(ResolverContext context)
		{
			return attributes.get(context.getCacheKey());
		}

		@Override
		protected void doPutContent(ResolverContext context, ContentRef value)
		{
			content.put(context.getCacheKey(), value);
		}

		@Override
		protected void doPutAttributes(ResolverContext context, AttributeSet value)
		{
			attributes.put(context.getCacheKey(), value);
		}
	}
}
