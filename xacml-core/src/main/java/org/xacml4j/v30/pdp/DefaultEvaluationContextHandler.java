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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.*;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.XPathProvider;

import java.util.*;

class DefaultEvaluationContextHandler
	implements EvaluationContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(DefaultEvaluationContextHandler.class);

	private PolicyInformationPoint pip;

	private RequestContextCallback requestCallback;

	private Map<AttributeSelectorKey, BagOfAttributeValues> selectorCache;
	private Map<AttributeDesignatorKey, BagOfAttributeValues> designatorCache;

	private Stack<AttributeDesignatorKey> designatorResolutionStack;
	private Stack<AttributeSelectorKey> selectorResolutionStack;

	DefaultEvaluationContextHandler(
			RequestContextCallback requestCallback,
			PolicyInformationPoint pip,
			Supplier<Map<AttributeDesignatorKey, BagOfAttributeValues>> designatorCacheSup,
			Supplier<Map<AttributeSelectorKey, BagOfAttributeValues>> selectorCacheSup)
	{
		Preconditions.checkNotNull(pip);
		Preconditions.checkNotNull(requestCallback);
		this.pip = pip;
		this.requestCallback = requestCallback;
		this.selectorResolutionStack = new Stack<>();
		this.designatorResolutionStack = new Stack<>();
		this.designatorCache = designatorCacheSup.get();
		this.selectorCache = selectorCacheSup.get();
	}

	DefaultEvaluationContextHandler(
			RequestContextCallback requestCallback,
			PolicyInformationPoint pip)
	{
		this(requestCallback, pip,
				()->new HashMap<>(),
				()->new HashMap<>());
	}

	public Map<AttributeDesignatorKey, BagOfAttributeValues> getResolvedDesignators(){
		return Collections.unmodifiableMap(designatorCache);
	}

	public Map<AttributeSelectorKey, BagOfAttributeValues> getResolvedSelectors(){
		return Collections.unmodifiableMap(selectorCache);
	}

	@Override
	public java.util.Optional<BagOfAttributeValues> resolve(
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

	private Optional<BagOfAttributeValues> doSelectorResolve(EvaluationContext context, AttributeReferenceKey ref)
	{
		Preconditions.checkArgument(ref instanceof AttributeSelectorKey);
		return requestCallback.resolve((AttributeSelectorKey) ref)
				.or(()->doPipResolve(context, (AttributeSelectorKey)ref));
	}

	private Optional<BagOfAttributeValues> doDesignatorResolve(EvaluationContext context, AttributeReferenceKey ref)
	{
		Preconditions.checkArgument(ref instanceof AttributeDesignatorKey);
		return requestCallback.resolve((AttributeDesignatorKey) ref)
				.or(()->doPipResolve(context, (AttributeDesignatorKey)ref));
	}

	private Optional<BagOfAttributeValues> doPipResolve(EvaluationContext context, AttributeDesignatorKey key)
	{
		Preconditions.checkState(
				!designatorResolutionStack.contains(key),
				"Cyclic designator=\"%s\" resolution detected", key);
		try
		{
			designatorResolutionStack.push(key);
			Optional<BagOfAttributeValues> v =  pip.resolve(context, key);
			v.ifPresent(bag -> designatorCache.putIfAbsent(key, bag));
			return v;

		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw AttributeReferenceEvaluationException
					.forDesignator(key, e);
		}finally{
			designatorResolutionStack.pop();
		}
	}


	private java.util.Optional<BagOfAttributeValues> doPipResolve(
			final EvaluationContext context,
			final AttributeSelectorKey ref) throws EvaluationException
	{
		Preconditions.checkState(
				!selectorResolutionStack.contains(ref),
				"Cyclic designator=\"%s\" resolution detected", ref);
		try
		{
			selectorResolutionStack.push(ref);
			return Optional.empty();
		}
		catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			context.setEvaluationStatus(e.getStatus());
			throw e;
		}
		catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw AttributeReferenceEvaluationException
					.forSelector(ref, e);
		}
		finally {
			selectorResolutionStack.pop();
		}
	}

	public <C extends Content> Optional<C> getContent(Optional<CategoryId> id){
		return requestCallback
				.getEntity(id)
				.flatMap((
						entity -> entity.getContent()));
	}

}
