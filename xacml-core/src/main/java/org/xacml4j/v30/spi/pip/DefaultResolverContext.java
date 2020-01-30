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

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

import java.util.function.Supplier;

import com.google.common.base.MoreObjects;
import org.xacml4j.v30.*;



final class DefaultResolverContext implements
		ResolverContext
{
	private Optional<Content.Type> contentType;
	private EvaluationContext context;
	private Map<AttributeReferenceKey, BagOfAttributeValues> resolvedKeys;
	private Resolver resolver;

	public DefaultResolverContext(
			Resolver resolver,
			EvaluationContext context,
			Supplier<Optional<Content.Type>> supplier) throws EvaluationException {
		this.resolver = Objects.requireNonNull(resolver, Resolver.class.getSimpleName());
		this.context = Objects.requireNonNull(context, ResolverDescriptor.class.getSimpleName());
		this.contentType = Objects.requireNonNull(supplier, Supplier.class.getSimpleName()).get();
	}

	public DefaultResolverContext(
			Resolver resolver,
			EvaluationContext context){
		this(resolver, context, ()->Optional.empty());
	}

	public EvaluationContext getEvaluationContext(){
		return context;
	}

		@Override
	public Optional<Content.Type> getContentType() {
		return  contentType;
	}

	@Override
	public ZonedDateTime getCurrentDateTime() {
		return context.getCurrentDateTime();
	}

	@Override
	public Clock getClock(){
		return context.getClock();
	}

	@Override
	public ResolverDescriptor getDescriptor(){
		return resolver.getDescriptor();
	}

	public Map<AttributeReferenceKey, BagOfAttributeValues> getResolvedKeys(){
		if(resolvedKeys == null){
			this.resolvedKeys = resolver.getDescriptor().resolveKeyRefs(this);
		}
		return resolvedKeys;
	}

	@Override
	public Optional<BagOfAttributeValues> resolve(AttributeReferenceKey key)
	{
		BagOfAttributeValues cachedValue =  resolvedKeys != null?resolvedKeys.get(key):null;
		if(cachedValue != null){
			return Optional.ofNullable(cachedValue);
		}
		Optional<BagOfAttributeValues> resolvedValue = context.resolve(key);
		resolvedValue.ifPresent((v)->{
			if(resolvedKeys == null){
				this.resolvedKeys = new HashMap<>();
			}
			resolvedKeys.putIfAbsent(key, v);
		});
		return resolvedValue;
	}

	public Supplier<Optional<BagOfAttributeValues>> resolveRef(AttributeReferenceKey referenceKey){
		Optional<BagOfAttributeValues> v = resolve(referenceKey);
		v.ifPresent(bagOfAttributeValues -> resolvedKeys.put(referenceKey, bagOfAttributeValues));
		return ()->v;
	}

	public boolean isCacheable(){
		return resolver
				.getDescriptor()
				.isCacheable();
	}

	@Override
	public <T extends Resolver> T getResolver() {
		return (T)resolver;
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("context", context)
				.add("resolver", resolver)
				.add("resolvedKeys", resolvedKeys)
				.toString();
	}
}
