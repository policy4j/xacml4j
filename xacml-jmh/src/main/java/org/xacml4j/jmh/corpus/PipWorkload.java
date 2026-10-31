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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.AttributeSet;
import org.xacml4j.v30.spi.pip.ContentRef;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.PolicyInformationPointCacheProvider;
import org.xacml4j.v30.spi.pip.ResolverCacheKey;
import org.xacml4j.v30.spi.pip.ResolverContext;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The fixture-owned PIP and simulated backend for the {@link PipCorpus}. Every value read needs
 * the backend; the bulk reads of the independent case run serially or on the fixture's executor
 * ({@code pipMode}), and the reads of the parallel-attributes case run through the information
 * point's own runner on that executor (design D26).
 */
public final class PipWorkload implements AutoCloseable
{
	private static final AttributeDesignatorKey IDENTITY = key(CategoryId.SUBJECT_ACCESS, PipCorpus.IDENTITY);
	private static final AttributeDesignatorKey TENANT = key(CategoryId.RESOURCE, PipCorpus.TENANT);
	/** The cases whose resolvers are all cacheable, so a warm cache answers them without the backend. */
	private static final List<String> CACHED_CASES = List.of("independent", "parallel-attributes", "dependent", "shared",
			"shared-static", "selector");
	private final ExecutorService executor;
	private final Semaphore slots;
	private final int count;
	private final long latencyMicros;
	private final boolean parallel;
	private final boolean cached;
	private final PolicyInformationPoint pip;
	private volatile boolean backendAvailable = true;

	private PipWorkload(int count, long latencyMicros, String mode, String cache, int maxInFlight){
		if(count < 1 || count > 64) throw new IllegalArgumentException("PIP fan-out must be from 1 to 64");
		if(latencyMicros < 0) throw new IllegalArgumentException("PIP latency must not be negative");
		if(maxInFlight < 1) throw new IllegalArgumentException("PIP max in-flight must be positive");
		this.parallel = switch(mode){
			case "serial" -> false;
			case "parallel" -> true;
			default -> throw new IllegalArgumentException("Unknown PIP mode: " + mode);
		};
		this.cached = switch(cache){
			case "off" -> false;
			case "on" -> true;
			default -> throw new IllegalArgumentException("Unknown PIP cache: " + cache);
		};
		this.count = count;
		this.latencyMicros = latencyMicros;
		this.slots = new Semaphore(maxInFlight);
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
		try{
			ResolverRegistry registry = registry();
			PolicyInformationPoint.Builder<?> builder = PolicyInformationPoint.builder("jmh-pip").registry(registry)
					.resolverExecutor(executor);
			this.pip = (cached ? builder.cacheProvider(new CacheProvider()) : builder.noCacheProvider()).build();
		}catch(RuntimeException | Error failure){
			executor.close();
			throw failure;
		}
	}

	public static PipWorkload open(int count, long latencyMicros, String mode, String cache, int maxInFlight){
		return new PipWorkload(count, latencyMicros, mode, cache, maxInFlight);
	}

	public PolicyInformationPoint pip(){
		return pip;
	}

	/** Checks that a warm cache answers every case with only cacheable resolvers without the backend. */
	public void verifyCache(PdpFixture fixture){
		if(!cached) return;
		backendAvailable = false;
		try{
			for(String id : CACHED_CASES){
				CorpusRequest request = fixture.requests(id).get(0);
				PipCorpus.check(fixture.corpus(), request, fixture.pdp().decide(request.request()))
						.ifPresent(failure -> { throw new IllegalStateException(
								"PIP cache did not retain the warm result of \"" + id + "\": " + failure); });
			}
		}finally{
			backendAvailable = true;
		}
	}

	private ResolverRegistry registry(){
		ResolverRegistry.Builder<?> builder = ResolverRegistry.builder().withDefaultResolvers();
		builder.withAttributeResolver(stringResolver("case", PipCorpus.CASE, IDENTITY,
				context -> identity(context, IDENTITY)));
		builder.withAttributeResolver(AttributeResolverDescriptor.builder("bulk", "bulk", CategoryId.RESOURCE)
				.cache(3600).contextRef(IDENTITY).attribute(PipCorpus.BULK, XacmlTypes.STRING)
				.build(context -> {
					identity(context, IDENTITY);
					BagOfValues.Builder bag = XacmlTypes.STRING.bagBuilder();
					for(String value : readAll()) bag.attribute(XacmlTypes.STRING.ofAny(value));
					return Map.of(PipCorpus.BULK, bag.build());
				}));
		AttributeResolverDescriptor.Builder attributes = AttributeResolverDescriptor
				.builder("parallel-attributes", "parallel-attributes", CategoryId.RESOURCE).cache(3600).contextRef(IDENTITY);
		for(int i = 0; i < count; i++){
			int index = i;
			attributes.attribute(PipCorpus.attributeId(index), XacmlTypes.STRING, context -> string(read(index)));
		}
		builder.withAttributeResolver(attributes.buildParallel());
		builder.withAttributeResolver(stringResolver("tenant", PipCorpus.TENANT, IDENTITY,
				context -> readValue("tenant-" + identity(context, IDENTITY))));
		builder.withAttributeResolver(stringResolver("role", PipCorpus.ROLE, TENANT,
				context -> readValue(identity(context, TENANT).equals("tenant-dependent") ? "deny" : "unknown")));
		builder.withAttributeResolver(AttributeResolverDescriptor.builder("shared", "shared", CategoryId.RESOURCE)
				.cache(3600).contextRef(IDENTITY)
				.attribute(PipCorpus.SHARED, XacmlTypes.STRING).attribute(PipCorpus.SHARED_PEER, XacmlTypes.STRING)
				.build(context -> {
					identity(context, IDENTITY);
					String value = readValue("allow");
					return Map.of(PipCorpus.SHARED, string(value), PipCorpus.SHARED_PEER, string(value + "-peer"));
				}));
		// No context key: the key of the call is its resolver, so the set the decision keeps serves
		// the peer with the cache off too (task 30)
		builder.withAttributeResolver(AttributeResolverDescriptor.builder("shared-static", "shared-static", CategoryId.RESOURCE)
				.cache(3600)
				.attribute(PipCorpus.SHARED_STATIC, XacmlTypes.STRING).attribute(PipCorpus.SHARED_STATIC_PEER, XacmlTypes.STRING)
				.build(context -> {
					String value = readValue("fixed");
					return Map.of(PipCorpus.SHARED_STATIC, string(value), PipCorpus.SHARED_STATIC_PEER, string(value + "-peer"));
				}));
		builder.withAttributeResolver(AttributeResolverDescriptor.builder("missing", "missing", CategoryId.RESOURCE)
				.cache(3600).contextRef(IDENTITY).attribute(PipCorpus.MISSING, XacmlTypes.STRING)
				.build(context -> { identity(context, IDENTITY); pause(false); return Map.of(); }));
		builder.withAttributeResolver(failing("failure", PipCorpus.FAILURE));
		builder.withAttributeResolver(failing("fallback-failure", PipCorpus.FALLBACK));
		builder.withAttributeResolver(stringResolver("fallback", PipCorpus.FALLBACK, IDENTITY,
				context -> { identity(context, IDENTITY); return readValue("allow"); }));
		builder.withAttributeResolver(AttributeResolverDescriptor.builder("content", "content", CategoryId.RESOURCE)
				.cache(3600).contextRef(IDENTITY).attribute(PipCorpus.CONTENT, XacmlTypes.ENTITY)
				.build(context -> {
					identity(context, IDENTITY);
					pause(true);
					Entity entity = Entity.builder().xmlContent(PipCorpus.XML)
							.attribute(Attribute.builder(PipCorpus.CONTENT)
									.value(XacmlTypes.XPATH.ofAny("/profile", CategoryId.RESOURCE)).build()).build();
					return Map.of(PipCorpus.CONTENT, XacmlTypes.ENTITY.bagOf(entity));
				}));
		return builder.build();
	}

	private static AttributeResolverDescriptor stringResolver(String id, String attribute, AttributeDesignatorKey dependency,
			java.util.function.Function<ResolverContext, String> value){
		return AttributeResolverDescriptor.builder(id, id, CategoryId.RESOURCE)
				.cache(3600).contextRef(dependency).attribute(attribute, XacmlTypes.STRING)
				.build(context -> Map.of(attribute, string(value.apply(context))));
	}

	private static BagOfValues string(String value){
		return XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny(value));
	}

	private AttributeResolverDescriptor failing(String id, String attribute){
		return AttributeResolverDescriptor.builder(id, id, CategoryId.RESOURCE)
				.noCache().contextRef(IDENTITY).attribute(attribute, XacmlTypes.STRING)
				.build(context -> { identity(context, IDENTITY); pause(false); throw new IllegalStateException("simulated backend failure"); });
	}

	private static AttributeDesignatorKey key(CategoryId category, String id){
		return AttributeDesignatorKey.builder(id).category(category).dataType(XacmlTypes.STRING).build();
	}

	private static String identity(ResolverContext context, AttributeDesignatorKey key){
		return ((StringVal)context.resolveContextRef(key).orElseThrow().single().orElseThrow()).get();
	}

	private List<String> readAll(){
		if(!parallel){
			List<String> values = new ArrayList<>(count);
			for(int i = 0; i < count; i++) values.add(read(i));
			return values;
		}
		return readParallel();
	}

	private List<String> readParallel(){
		List<Callable<String>> calls = new ArrayList<>(count);
		for(int i = 0; i < count; i++){
			int index = i;
			calls.add(() -> read(index));
		}
		try{
			List<String> values = new ArrayList<>(count);
			for(Future<String> result : executor.invokeAll(calls)) values.add(result.get());
			return values;
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
			throw new IllegalStateException("PIP backend interrupted", e);
		}catch(ExecutionException e){
			throw new IllegalStateException("PIP backend failed", e.getCause());
		}
	}

	private String read(int index){
		pause(true);
		return "v" + index;
	}

	private String readValue(String value){
		pause(true);
		return value;
	}

	private void pause(boolean needsBackend){
		try{
			slots.acquire();
			try{
				if(needsBackend && !backendAvailable) throw new IllegalStateException("simulated backend unavailable");
				if(latencyMicros > 0) Thread.sleep(latencyMicros / 1000, (int)(latencyMicros % 1000) * 1000);
			}finally{
				slots.release();
			}
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
			throw new IllegalStateException("PIP backend interrupted", e);
		}
	}

	@Override
	public void close(){
		executor.close();
	}

	private static final class CacheProvider implements PolicyInformationPointCacheProvider
	{
		private final Cache<ResolverCacheKey, AttributeSet> attributes = CacheBuilder.newBuilder()
				.maximumSize(1024).expireAfterWrite(1, TimeUnit.HOURS).build();
		private final Cache<ResolverCacheKey, ContentRef> content = CacheBuilder.newBuilder()
				.maximumSize(1024).expireAfterWrite(1, TimeUnit.HOURS).build();

		@Override
		public Optional<ContentRef> getContent(ResolverContext context){
			return Optional.ofNullable(content.getIfPresent(context.getCacheKey()));
		}

		@Override
		public void putContent(ResolverContext context, ContentRef value){
			content.put(context.getCacheKey(), value);
		}

		@Override
		public Optional<AttributeSet> getAttributes(ResolverContext context){
			return Optional.ofNullable(attributes.getIfPresent(context.getCacheKey()));
		}

		@Override
		public void putAttributes(ResolverContext context, AttributeSet value){
			attributes.put(context.getCacheKey(), value);
		}
	}
}
