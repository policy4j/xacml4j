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
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.types.Entity;
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
		try{
			return requestCallback.getEntity(ref.getCategory())
			                      .flatMap(e-> resolveSelector(e, selectorKey))
			                      .or(()->doPipResolve(context, selectorKey));
		}catch(PathEvaluationException e){
			// The outcome of the engine's own evaluation of the selector path
			// on request content or on a resolved entity. The status message is
			// visible in the response, so the text of the XPath engine is not
			// passed on: a syntax error keeps its status code, and every other
			// path error is a processing error, whatever MustBePresent is.
			if(e.getStatus().getStatusCode().isSyntaxError()){
				throw logged(AttributeReferenceEvaluationException.forSyntaxError(selectorKey, e));
			}
			throw failed(selectorKey, e);
		}
	}

	private Optional<BagOfValues> resolveSelector(Entity entity, AttributeSelectorKey selectorKey)
	{
		if(selectorKey.getContextSelectorId() == null){
			return entity.<Content>getContent().flatMap(content->content.resolve(selectorKey));
		}
		return entity.resolve(selectorKey);
	}

	private Optional<BagOfValues> doDesignatorResolve(EvaluationContext context, AttributeReferenceKey ref)
	{
		Preconditions.checkArgument(ref instanceof AttributeDesignatorKey);
		AttributeDesignatorKey designatorKey = (AttributeDesignatorKey)ref;
		Optional<BagOfValues> v = requestCallback.getEntity(ref.getCategory())
		                                         .flatMap(e-> e.resolve(designatorKey.getAttributeId(),
		                                                              designatorKey.getDataType(), designatorKey.getIssuer()))
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
			// A failure of the information point is never a missing attribute.
			// Its own status and message are not passed on, because a status
			// message is visible in the response.
			throw failed(key, e);
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
			Optional<BagOfValues> v = resolveViaPip(context, ref);
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
				          .map(Entity.class::cast)
						.flatMap(e->e.resolve(ref));
			}
			v.ifPresent(bag -> selectorCache.putIfAbsent(ref, bag));
			return v;
		}
		catch(PathEvaluationException e){
			// Only the engine's own evaluation of the selector on a resolved
			// entity arrives here, because resolveViaPip maps every exception
			// of the information point. doSelectorResolve maps it.
			throw e;
		}
		catch(Exception e){
			// A failure is never a missing attribute.
			throw failed(ref, e);
		}
		finally {
			selectorResolutionStack.pop();
		}
	}

	/**
	 * The boundary to the information point for a selector. Every exception
	 * is a resolver failure: the information point can not choose the status
	 * code or the status message, because both are visible in the response.
	 */
	private Optional<BagOfValues> resolveViaPip(EvaluationContext context, AttributeSelectorKey ref)
	{
		try{
			return pip.resolve(context, ref);
		}catch(Exception e){
			throw failed(ref, e);
		}
	}

	/**
	 * Maps a failure to a processing error and logs it. Nothing is called
	 * on the cause before the mapped failure exists, and the cause is not
	 * given to the logger: it is foreign code, and an exception from its
	 * {@code getMessage()} must not replace the mapped failure.
	 */
	private static AttributeReferenceEvaluationException failed(AttributeReferenceKey ref, Throwable cause)
	{
		return logged(AttributeReferenceEvaluationException.forResolverFailure(ref, cause));
	}

	private static AttributeReferenceEvaluationException logged(AttributeReferenceEvaluationException failure)
	{
		LOG.debug("{}", failure.getMessage());
		return failure;
	}

	@Override
	public Optional<Entity> getEntity(CategoryId id){
		return requestCallback.getEntity(id);
	}

}
