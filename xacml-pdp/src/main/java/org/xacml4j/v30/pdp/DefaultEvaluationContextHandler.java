package org.xacml4j.v30.pdp;

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

import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.Entity;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.types.EntityValue;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

final class DefaultEvaluationContextHandler
		implements EvaluationContextHandler
{
	private final static Logger LOG = LoggerFactory.getLogger(DefaultEvaluationContextHandler.class);

	private PolicyInformationPoint pip;

	private RequestContextCallback requestCallback;

	private Map<AttributeSelectorKey, BagOfValues> selectorCache;
	private Map<AttributeDesignatorKey, BagOfValues> designatorCache;

	private Deque<AttributeDesignatorKey> designatorResolutionStack;
	private Deque<AttributeSelectorKey> selectorResolutionStack;

	DefaultEvaluationContextHandler(
			RequestContextCallback requestCallback,
			PolicyInformationPoint pip,
			Supplier<Map<AttributeDesignatorKey, BagOfValues>> designatorCacheSup,
			Supplier<Map<AttributeSelectorKey, BagOfValues>> selectorCacheSup)
	{
		Preconditions.checkNotNull(pip);
		Preconditions.checkNotNull(requestCallback);
		this.pip = pip;
		this.requestCallback = requestCallback;
		this.selectorResolutionStack = new ConcurrentLinkedDeque<>();
		this.designatorResolutionStack = new ConcurrentLinkedDeque<>();
		this.designatorCache = designatorCacheSup.get();
		this.selectorCache = selectorCacheSup.get();
	}

	DefaultEvaluationContextHandler(
			RequestContextCallback requestCallback,
			PolicyInformationPoint pip)
	{
		this(requestCallback, pip,
				()->new ConcurrentHashMap<>(),
				()->new ConcurrentHashMap<>());
	}

	public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators(){
		return Collections.unmodifiableMap(designatorCache);
	}

	public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors(){
		return Collections.unmodifiableMap(selectorCache);
	}

	@Override
	public java.util.Optional<BagOfValues> resolve(
			EvaluationContext context,
			AttributeReferenceKey ref)
		throws EvaluationException
	{
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(ref);
		if(ref instanceof AttributeDesignatorKey){
			return Optional.ofNullable(designatorCache.get(ref))
					.or(()->doDesignatorResolve(context, ref));
		}
		if(ref instanceof AttributeSelectorKey){
			return Optional.ofNullable(selectorCache.get(ref))
					.or(()->doSelectorResolve(context, ref));
		}
		return Optional.empty();
	}

	private Optional<BagOfValues> doSelectorResolve(EvaluationContext context, AttributeReferenceKey ref)
	{
		Preconditions.checkArgument(ref instanceof AttributeSelectorKey);
		AttributeSelectorKey selectorKey = (AttributeSelectorKey)ref;
		Optional<BagOfValues> v = requestCallback.getEntity(ref.getCategory())
		                                         .flatMap(e-> e.resolve(selectorKey))
		                                         .or(()->doPipResolve(context, selectorKey));
		return v;
	}

	private Optional<BagOfValues> doDesignatorResolve(EvaluationContext context, AttributeReferenceKey ref)
	{
		Preconditions.checkArgument(ref instanceof AttributeDesignatorKey);
		AttributeDesignatorKey designatorKey = (AttributeDesignatorKey)ref;
		Optional<BagOfValues> v = requestCallback.getEntity(ref.getCategory())
		                                         .flatMap(e-> e.resolve(designatorKey))
		                                         .or(()->doPipResolve(context, (AttributeDesignatorKey)ref));
		return v;
	}

	private Optional<BagOfValues> doPipResolve(EvaluationContext context, AttributeDesignatorKey key)
	{
		if(designatorResolutionStack.contains(key)){
			LOG.warn("Cyclic designator=\"{}\" resolution detected", key);
			return Optional.empty();
		}
		try
		{
			designatorResolutionStack.push(key);
			Optional<BagOfValues> v =  pip.resolve(context, key);
			v.ifPresent(bag -> designatorCache.putIfAbsent(key, bag));
			return v;

		}catch(Exception e){
			LOG.debug(e.getMessage(), e);
			return Optional.empty();
		}finally{
			designatorResolutionStack.pop();
		}
	}

	private java.util.Optional<BagOfValues> doPipResolve(
			final EvaluationContext context,
			final AttributeSelectorKey ref) throws EvaluationException
	{
		if(selectorResolutionStack.contains(ref)){
			LOG.warn("Cyclic selector=\"{}\" resolution detected", ref);
			return Optional.empty();
		}
		try
		{
			selectorResolutionStack.push(ref);
			Optional<BagOfValues> v = pip.resolve(context, ref);
			if(!v.isPresent() &&
					ref.getContextSelectorId() != null){
				AttributeDesignatorKey entitySelector =
						AttributeDesignatorKey
								.builder()
								.dataType(XacmlTypes.ENTITY)
								.attributeId(ref.getContextSelectorId())
								.category(ref.getCategory())
								.build();
				v = resolve(context, entitySelector)
				          .flatMap(b->b.single())
				          .map(EntityValue.class::cast)
				          .map(EntityValue::value)
						.flatMap(e->e.resolve(ref));
			}
			v.ifPresent(bag -> selectorCache.putIfAbsent(ref, bag));
			return v;
		}
		catch(Exception e){
			LOG.debug(e.getMessage(), e);
			return Optional.empty();
		}
		finally {
			selectorResolutionStack.pop();
		}
	}

	public <C extends Content> Optional<C> getContent(CategoryId id){
		return requestCallback
				.getEntity(id)
				.flatMap((
						entity -> entity.getContent()));
	}

}
