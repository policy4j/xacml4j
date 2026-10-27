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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
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

	// The information point caches and the cycle stacks are created on their first use and
	// published with a compare-and-set (change optimize-pdp-evaluation, D9). A decision answered
	// from the request alone creates no stack; it creates the designator cache when the result
	// reads the resolved attributes, because that view must stay backed by the cache (rv008).
	private static final VarHandle SELECTOR_CACHE = handle("selectorCache", Map.class);
	private static final VarHandle DESIGNATOR_CACHE = handle("designatorCache", Map.class);
	private static final VarHandle DESIGNATOR_STACK = handle("designatorResolutionStack", Deque.class);
	private static final VarHandle SELECTOR_STACK = handle("selectorResolutionStack", Deque.class);

	private PolicyInformationPoint pip;

	private RequestContextCallback requestCallback;

	private final Supplier<Map<AttributeDesignatorKey, BagOfValues>> designatorCacheSup;
	private final Supplier<Map<AttributeSelectorKey, BagOfValues>> selectorCacheSup;
	private volatile Map<AttributeSelectorKey, BagOfValues> selectorCache;
	private volatile Map<AttributeDesignatorKey, BagOfValues> designatorCache;

	/**
	 * Bags found in the request, by designator, for this one decision: XACML 3.0 section
	 * 7.3.5 asks every later test of an attribute to use the bag first tested. Request
	 * results only; information point results stay in {@link #designatorCache}, which the
	 * response's resolved attributes come from. Neither absence nor a failure is kept.
	 */
	private final Map<AttributeDesignatorKey, BagOfValues> requestDesignators = new ConcurrentHashMap<>();

	private volatile Deque<AttributeDesignatorKey> designatorResolutionStack;
	private volatile Deque<AttributeSelectorKey> selectorResolutionStack;

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
		this.designatorCacheSup = Preconditions.checkNotNull(designatorCacheSup);
		this.selectorCacheSup = Preconditions.checkNotNull(selectorCacheSup);
	}

	private static VarHandle handle(String field, Class<?> type)
	{
		try{
			return MethodHandles.lookup().findVarHandle(DefaultEvaluationContextHandler.class, field, type);
		}catch(ReflectiveOperationException e){
			throw new ExceptionInInitializerError(e);
		}
	}

	/** @return the given new value, or the value another thread set first */
	private <T> T created(VarHandle field, T value)
	{
		@SuppressWarnings("unchecked")
		T witness = (T)field.compareAndExchange(this, null, value);
		return witness != null ? witness : value;
	}

	private Map<AttributeDesignatorKey, BagOfValues> designatorCache()
	{
		Map<AttributeDesignatorKey, BagOfValues> m = designatorCache;
		return m != null ? m : created(DESIGNATOR_CACHE, designatorCacheSup.get());
	}

	private Map<AttributeSelectorKey, BagOfValues> selectorCache()
	{
		Map<AttributeSelectorKey, BagOfValues> m = selectorCache;
		return m != null ? m : created(SELECTOR_CACHE, selectorCacheSup.get());
	}

	private Deque<AttributeDesignatorKey> designatorResolutionStack()
	{
		Deque<AttributeDesignatorKey> d = designatorResolutionStack;
		return d != null ? d : created(DESIGNATOR_STACK, new ConcurrentLinkedDeque<>());
	}

	private Deque<AttributeSelectorKey> selectorResolutionStack()
	{
		Deque<AttributeSelectorKey> d = selectorResolutionStack;
		return d != null ? d : created(SELECTOR_STACK, new ConcurrentLinkedDeque<>());
	}

	DefaultEvaluationContextHandler(
			RequestContextCallback requestCallback,
			PolicyInformationPoint pip)
	{
		this(requestCallback, pip,
				()->new ConcurrentHashMap<>(),
				()->new ConcurrentHashMap<>());
	}

	/**
	 * The bags from the information point, as the read-only view of the cache. Asking for the
	 * view creates the cache, so a caller that keeps the map or its entry set sees later bags,
	 * as before the caches were created on first use (reviews RVC1, RVC2, RVC2R).
	 */
	public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators(){
		return Collections.unmodifiableMap(designatorCache());
	}

	public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors(){
		return Collections.unmodifiableMap(selectorCache());
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
			AttributeDesignatorKey key = (AttributeDesignatorKey)ref;
			Map<AttributeDesignatorKey, BagOfValues> pipBags = designatorCache;
			BagOfValues bag = pipBags != null ? pipBags.get(key) : null;
			if(bag == null){
				bag = requestDesignators.get(key);
			}
			return bag != null ? Optional.of(bag) : doDesignatorResolve(context, key);
		}
		if(ref instanceof AttributeSelectorKey){
			Map<AttributeSelectorKey, BagOfValues> pipBags = selectorCache;
			BagOfValues bag = pipBags != null ? pipBags.get(ref) : null;
			return bag != null ? Optional.of(bag) : doSelectorResolve(context, ref);
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

	private Optional<BagOfValues> doDesignatorResolve(EvaluationContext context, AttributeDesignatorKey key)
	{
		Optional<Entity> entity = requestCallback.getEntity(key.getCategory());
		if(entity.isPresent()){
			Optional<BagOfValues> found = entity.get().resolve(key.getAttributeId(), key.getDataType(), key.getIssuer());
			if(found.isPresent()){
				// The first bag wins, so that every test of the attribute sees one bag.
				BagOfValues first = requestDesignators.putIfAbsent(key, found.get());
				return first != null ? Optional.of(first) : found;
			}
		}
		return doPipResolve(context, key);
	}

	private Optional<BagOfValues> doPipResolve(EvaluationContext context, AttributeDesignatorKey key)
	{
		Deque<AttributeDesignatorKey> stack = designatorResolutionStack();
		if(stack.contains(key)){
			LOG.warn("Cyclic designator=\"{}\" resolution detected", key);
			return Optional.empty();
		}
		try
		{
			stack.push(key);
			Optional<BagOfValues> v =  pip.resolve(context, key);
			if(v.isPresent()){
				designatorCache().putIfAbsent(key, v.get());
			}
			return v;

		}catch(Exception e){
			// A failure of the information point is never a missing attribute.
			// Its own status and message are not passed on, because a status
			// message is visible in the response.
			throw failed(key, e);
		}finally{
			stack.pop();
		}
	}

	private java.util.Optional<BagOfValues> doPipResolve(
			final EvaluationContext context,
			final AttributeSelectorKey ref) throws EvaluationException
	{
		Deque<AttributeSelectorKey> stack = selectorResolutionStack();
		if(stack.contains(ref)){
			LOG.warn("Cyclic selector=\"{}\" resolution detected", ref);
			return Optional.empty();
		}
		try
		{
			stack.push(ref);
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
			if(v.isPresent()){
				selectorCache().putIfAbsent(ref, v.get());
			}
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
			stack.pop();
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
