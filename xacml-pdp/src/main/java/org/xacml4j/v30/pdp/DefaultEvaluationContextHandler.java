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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.spi.observe.AttributeSource;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

final class DefaultEvaluationContextHandler
		implements EvaluationContextHandler
{
	private final static Logger LOG = LoggerFactory.getLogger(DefaultEvaluationContextHandler.class);

	// The information point caches, the bags from the request, the resolutions in progress, and
	// the information point of the decision are created on their first use and published with a
	// compare-and-set (change optimize-pdp-evaluation, D9, task 30, cycle AH). The state lives
	// in one holder, created with its first member (cycle AV). A decision that
	// tests no attribute creates none of them; it creates the designator cache when the result
	// reads the resolved attributes, because that view must stay backed by the cache (rv008).
	private static final VarHandle STATE = handle(DefaultEvaluationContextHandler.class, "state", ResolutionState.class);
	private static final VarHandle SELECTOR_CACHE = handle(ResolutionState.class, "selectorCache", Map.class);
	private static final VarHandle DESIGNATOR_CACHE = handle(ResolutionState.class, "designatorCache", Map.class);
	private static final VarHandle REQUEST_DESIGNATORS = handle(ResolutionState.class, "requestDesignators", ConcurrentMap.class);
	private static final VarHandle FLIGHTS = handle(ResolutionState.class, "flights", Map.class);
	private static final VarHandle SESSION = handle(ResolutionState.class, "session", PolicyInformationPoint.class);

	/**
	 * The information point resolutions on the way down on the current thread, the innermost
	 * first, bound around every call of the information point. A frame names its handler, so a
	 * nested decision that resolves the same key inside a resolver is not a cycle (design D27).
	 */
	private static final ScopedValue<Frame> ANCESTRY = ScopedValue.newInstance();

	private final PolicyInformationPoint pip;

	/** The request of the decision, which answers the categories (cycle AH: no callback per decision) */
	private final RequestContext request;

	private volatile ResolutionState state;

	DefaultEvaluationContextHandler(
			RequestContext request,
			PolicyInformationPoint pip)
	{
		Preconditions.checkNotNull(pip);
		Preconditions.checkNotNull(request);
		this.pip = pip;
		this.request = request;
	}

	private static VarHandle handle(Class<?> owner, String field, Class<?> type)
	{
		try{
			return MethodHandles.lookup().findVarHandle(owner, field, type);
		}catch(ReflectiveOperationException e){
			throw new ExceptionInInitializerError(e);
		}
	}

	/** @return the given new value, or the value another thread set first */
	private static <T> T created(Object owner, VarHandle field, T value)
	{
		@SuppressWarnings("unchecked")
		T witness = (T)field.compareAndExchange(owner, null, value);
		return witness != null ? witness : value;
	}

	private ResolutionState state()
	{
		ResolutionState s = state;
		return s != null ? s : created(this, STATE, new ResolutionState());
	}

	private Map<AttributeDesignatorKey, BagOfValues> designatorCache()
	{
		ResolutionState s = state();
		Map<AttributeDesignatorKey, BagOfValues> m = s.designatorCache;
		return m != null ? m : created(s, DESIGNATOR_CACHE, new ConcurrentHashMap<>());
	}

	private Map<AttributeSelectorKey, BagOfValues> selectorCache()
	{
		ResolutionState s = state();
		Map<AttributeSelectorKey, BagOfValues> m = s.selectorCache;
		return m != null ? m : created(s, SELECTOR_CACHE, new ConcurrentHashMap<>());
	}

	private Map<AttributeReferenceKey, CompletableFuture<Optional<BagOfValues>>> flights()
	{
		ResolutionState s = state();
		Map<AttributeReferenceKey, CompletableFuture<Optional<BagOfValues>>> m = s.flights;
		return m != null ? m : created(s, FLIGHTS, new ConcurrentHashMap<>());
	}

	/**
	 * The bags from the request, created on the first one with the default table: a smaller
	 * table grows in decisions that find more than six request bags, as rsa2008's do (cycle AH)
	 */
	private ConcurrentMap<AttributeDesignatorKey, Optional<BagOfValues>> requestDesignators()
	{
		ResolutionState s = state();
		ConcurrentMap<AttributeDesignatorKey, Optional<BagOfValues>> m = s.requestDesignators;
		return m != null ? m : created(s, REQUEST_DESIGNATORS, new ConcurrentHashMap<>());
	}

	private PolicyInformationPoint session()
	{
		ResolutionState s = state();
		PolicyInformationPoint point = s.session;
		return point != null ? point : created(s, SESSION, forDecision(pip));
	}

	/**
	 * The information point for one decision: the one that the point gives for it, or the point
	 * itself when it gives none, as a test double does.
	 */
	private static PolicyInformationPoint forDecision(PolicyInformationPoint pip)
	{
		PolicyInformationPoint s = pip.forDecision();
		return s != null ? s : pip;
	}

	private static final class ResolutionState
	{
		private volatile Map<AttributeSelectorKey, BagOfValues> selectorCache;
		private volatile Map<AttributeDesignatorKey, BagOfValues> designatorCache;

		/**
		 * Bags found in the request, by designator, for this one decision, kept only while the
		 * decision is observed: the observation tells a repeated test of an attribute from its first
		 * (design D34). XACML 3.0 section 7.3.5 asks every later test of an attribute to use the bag
		 * first tested, and the request entity itself keeps one bag per designator for the life of the
		 * request (cycle AF), so an unobserved decision needs no memo of its own (cycle AN). Request
		 * results only; information point results stay in {@link #designatorCache}, which the
		 * response's resolved attributes come from. Neither absence nor a failure is kept.
		 */
		private volatile ConcurrentMap<AttributeDesignatorKey, Optional<BagOfValues>> requestDesignators;

		/**
		 * The information point resolutions in progress, by key. The first caller of a key runs the
		 * attempt; a caller outside any resolution of this handler that arrives while it runs shares
		 * its outcome; a caller inside a resolution runs its own attempt. The entry goes on every
		 * exit (design D27).
		 */
		private volatile Map<AttributeReferenceKey, CompletableFuture<Optional<BagOfValues>>> flights;

		/**
		 * The information point of this decision, which may keep what the decision resolved for the
		 * length of the decision (task 30 of change optimize-pdp-evaluation).
		 */
		private volatile PolicyInformationPoint session;
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
	public boolean hasResolvedDesignators(){
		ResolutionState s = state;
		Map<AttributeDesignatorKey, BagOfValues> m = s != null ? s.designatorCache : null;
		return m != null && !m.isEmpty();
	}

	@Override
	public boolean usesRequestEntityForDesignators(){
		return true;
	}

	/**
	 * Resolves a designator or a selector as one attribute span of the observation of the
	 * decision; each return path names the source of its outcome (design D34)
	 */
	@Override
	public java.util.Optional<BagOfValues> resolve(
			EvaluationContext context,
			AttributeReferenceKey ref)
		throws EvaluationException
	{
		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(ref);
		if(!(ref instanceof AttributeDesignatorKey) && !(ref instanceof AttributeSelectorKey)){
			return Optional.empty();
		}
		DecisionObservation o = RootEvaluationContext.observationOf(context);
		if(!o.isEnabled()){
			return resolve(o, context, ref);
		}
		Object parent = o.attributeRequested(context, ref);
		Optional<BagOfValues> v;
		try{
			v = resolve(o, context, ref);
		}catch(Throwable t){
			o.attributeFailed(context, ref, parent, t);
			throw t;
		}
		o.attributeResolved(context, ref, parent, v);
		return v;
	}

	private Optional<BagOfValues> resolve(DecisionObservation o, EvaluationContext context, AttributeReferenceKey ref)
	{
		ResolutionState s = state;
		if(ref instanceof AttributeDesignatorKey key){
			Map<AttributeDesignatorKey, BagOfValues> pipBags = s != null ? s.designatorCache : null;
			BagOfValues bag = pipBags != null ? pipBags.get(key) : null;
			if(bag != null){
				o.resolvedFrom(AttributeSource.INFORMATION_POINT_MEMO);
				return Optional.of(bag);
			}
			ConcurrentMap<AttributeDesignatorKey, Optional<BagOfValues>> requestBags = s != null ? s.requestDesignators : null;
			Optional<BagOfValues> fromRequest = requestBags != null ? requestBags.get(key) : null;
			if(fromRequest != null){
				o.resolvedFrom(AttributeSource.REQUEST_MEMO);
				return fromRequest;
			}
			return doDesignatorResolve(o, context, key);
		}
		Map<AttributeSelectorKey, BagOfValues> pipBags = s != null ? s.selectorCache : null;
		BagOfValues bag = pipBags != null ? pipBags.get(ref) : null;
		if(bag != null){
			o.resolvedFrom(AttributeSource.INFORMATION_POINT_MEMO);
			return Optional.of(bag);
		}
		return doSelectorResolve(o, context, ref);
	}

	private Optional<BagOfValues> doSelectorResolve(DecisionObservation o, EvaluationContext context, AttributeReferenceKey ref)
	{
		Preconditions.checkArgument(ref instanceof AttributeSelectorKey);
		AttributeSelectorKey selectorKey = (AttributeSelectorKey)ref;
		try{
			Optional<BagOfValues> fromRequest = request.getEntity(ref.getCategory())
			                                           .flatMap(e-> resolveSelector(e, selectorKey));
			if(fromRequest.isPresent()){
				o.resolvedFrom(AttributeSource.REQUEST);
				return fromRequest;
			}
			return doPipResolve(o, context, selectorKey);
		}catch(PathEvaluationException e){
			// The engine's own evaluation of the selector path on request content; the
			// evaluation on a resolved entity is mapped inside the shared attempt (design D27)
			throw pathFailure(selectorKey, e);
		}
	}

	private Optional<BagOfValues> resolveSelector(Entity entity, AttributeSelectorKey selectorKey)
	{
		if(selectorKey.getContextSelectorId() == null){
			return entity.<Content>getContent().flatMap(content->content.resolve(selectorKey));
		}
		return entity.resolve(selectorKey);
	}

	private Optional<BagOfValues> doDesignatorResolve(DecisionObservation o, EvaluationContext context, AttributeDesignatorKey key)
	{
		Optional<Entity> entity = request.getEntity(key.getCategory());
		if(entity.isPresent()){
			// The entity keeps the bag of the designator, so a reused request costs no bag (cycle AF)
			Optional<BagOfValues> found = entity.get().resolve(key);
			if(found.isPresent()){
				if(!o.isEnabled()){
					return found;
				}
				// The first bag wins, so that every test of the attribute sees one bag.
				Optional<BagOfValues> first = requestDesignators().putIfAbsent(key, found);
				o.resolvedFrom(first != null ? AttributeSource.REQUEST_MEMO : AttributeSource.REQUEST);
				return first != null ? first : found;
			}
		}
		return doPipResolve(o, context, key);
	}

	private Optional<BagOfValues> doPipResolve(DecisionObservation o, EvaluationContext context, AttributeDesignatorKey key)
	{
		if(pip.hasNoResolver(key)){
			return missing(o);
		}
		return resolveOnce(o, key, ()->{
			try{
				return session().resolve(context, key);
			}catch(Exception e){
				// A failure of the information point is never a missing attribute.
				// Its own status and message are not passed on, because a status
				// message is visible in the response.
				throw failed(key, e);
			}
		});
	}

	private java.util.Optional<BagOfValues> doPipResolve(
			DecisionObservation o,
			final EvaluationContext context,
			final AttributeSelectorKey ref) throws EvaluationException
	{
		if(ref.getContextSelectorId() == null && pip.hasNoResolver(ref)){
			return missing(o);
		}
		return resolveOnce(o, ref, ()->{
			try{
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
				return v;
			}
			catch(PathEvaluationException e){
				// Only the engine's own evaluation of the selector on a resolved entity arrives
				// here, because resolveViaPip maps every exception of the information point.
				// Mapped inside the attempt, so every caller that shares it gets this one
				// exception (design D27).
				throw pathFailure(ref, e);
			}
			catch(Exception e){
				// A failure is never a missing attribute.
				throw failed(ref, e);
			}
		});
	}

	/**
	 * What an attempt of the information point gives for a key that none of its resolvers
	 * supplies, without the session, the flight, and the frame of an attempt (cycle AN): a missing
	 * attribute, so MustBePresent, the status, and the events of the decision are as for an attempt
	 */
	private static Optional<BagOfValues> missing(DecisionObservation o)
	{
		o.resolvedFrom(AttributeSource.INFORMATION_POINT);
		return Optional.empty();
	}

	/**
	 * The attempts of the information point for a key in one decision (design D27). The first
	 * caller of a key runs an attempt and publishes a present bag, so every caller gets the bag
	 * published first. A caller outside any resolution of this handler that arrives while an
	 * attempt runs waits for its outcome, absence and failure included; a caller inside one runs
	 * its own attempt instead, so that no attempt ever waits for another and no wait can form a
	 * cycle: two attempts of one key can run at a time, one of them nested in a resolution. A
	 * call after an attempt has ended starts a new one; nothing is kept of absence or of a
	 * failure. A key already on the way down on this thread is a cycle, which gives absence,
	 * as before.
	 */
	private Optional<BagOfValues> resolveOnce(DecisionObservation o, AttributeReferenceKey key, Supplier<Optional<BagOfValues>> attempt)
	{
		Frame ancestry = ANCESTRY.isBound() ? ANCESTRY.get() : null;
		if(Frame.contains(ancestry, this, key)){
			LOG.warn("Cyclic reference=\"{}\" resolution detected", key);
			o.resolvedFrom(AttributeSource.CYCLE);
			return Optional.empty();
		}
		Frame frame = new Frame(this, key, ancestry);
		CompletableFuture<Optional<BagOfValues>> flight = new CompletableFuture<>();
		Map<AttributeReferenceKey, CompletableFuture<Optional<BagOfValues>>> flights = flights();
		CompletableFuture<Optional<BagOfValues>> running = flights.putIfAbsent(key, flight);
		if(running != null){
			if(Frame.inside(ancestry, this)){
				return resolveWith(o, key, frame, attempt);
			}
			Optional<BagOfValues> shared = outcome(running);
			o.resolvedFrom(AttributeSource.INFORMATION_POINT);
			return shared;
		}
		try{
			Optional<BagOfValues> v = resolveWith(o, key, frame, attempt);
			flight.complete(v);
			return v;
		}catch(Throwable t){
			flight.completeExceptionally(t);
			throw t;
		}finally{
			flights.remove(key, flight);
		}
	}

	/**
	 * The bag published for the key while the caller was on its way here, or else the outcome
	 * of the attempt, run under its frame and published.
	 */
	private Optional<BagOfValues> resolveWith(DecisionObservation o, AttributeReferenceKey key, Frame frame,
			Supplier<Optional<BagOfValues>> attempt)
	{
		Optional<BagOfValues> published = published(key);
		if(published.isPresent()){
			o.resolvedFrom(AttributeSource.INFORMATION_POINT_MEMO);
			return published;
		}
		Optional<BagOfValues> v = publish(key, ScopedValue.where(ANCESTRY, frame).call(attempt::get));
		o.resolvedFrom(AttributeSource.INFORMATION_POINT);
		return v;
	}

	private Optional<BagOfValues> published(AttributeReferenceKey key)
	{
		ResolutionState s = state();
		Map<?, BagOfValues> bags = key instanceof AttributeDesignatorKey ? s.designatorCache : s.selectorCache;
		return bags != null ? Optional.ofNullable(bags.get(key)) : Optional.empty();
	}

	/** Publishes a present bag; the bag published first for a key is the one every caller gets */
	private Optional<BagOfValues> publish(AttributeReferenceKey key, Optional<BagOfValues> v)
	{
		if(!v.isPresent()){
			return v;
		}
		BagOfValues first = key instanceof AttributeDesignatorKey designator
				? designatorCache().putIfAbsent(designator, v.get())
				: selectorCache().putIfAbsent((AttributeSelectorKey)key, v.get());
		return first != null ? Optional.of(first) : v;
	}

	/** The outcome of an attempt: its bag or absence, or its failure as the attempt threw it */
	private static Optional<BagOfValues> outcome(CompletableFuture<Optional<BagOfValues>> flight)
	{
		try{
			return flight.join();
		}catch(CompletionException e){
			Throwable cause = e.getCause();
			if(cause instanceof RuntimeException failure){
				throw failure;
			}
			if(cause instanceof Error error){
				throw error;
			}
			throw e;
		}
	}

	/** One information point resolution on the way down: its handler, its key, and the one above it */
	private record Frame(DefaultEvaluationContextHandler handler, AttributeReferenceKey key, Frame parent)
	{
		static boolean contains(Frame frame, DefaultEvaluationContextHandler handler, AttributeReferenceKey key)
		{
			for(Frame f = frame; f != null; f = f.parent){
				if(f.handler == handler && f.key.equals(key)){
					return true;
				}
			}
			return false;
		}

		/** @return whether a resolution of the handler is on the way down */
		static boolean inside(Frame frame, DefaultEvaluationContextHandler handler)
		{
			for(Frame f = frame; f != null; f = f.parent){
				if(f.handler == handler){
					return true;
				}
			}
			return false;
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
			return session().resolve(context, ref);
		}catch(Exception e){
			throw failed(ref, e);
		}
	}

	/**
	 * Maps a failure to a processing error.
	 */
	private static AttributeReferenceEvaluationException failed(AttributeReferenceKey ref, Throwable cause)
	{
		return AttributeReferenceEvaluationException.forResolverFailure(ref, cause);
	}

	/**
	 * Maps a failure of the engine's own evaluation of a selector path, on request content or
	 * on a resolved entity: a syntax error keeps its status code, every other path error is a
	 * processing error, whatever MustBePresent is. The status message is visible in the
	 * response, so the text of the path engine is not passed on. The information point maps the
	 * path errors on its own content as resolver failures itself.
	 */
	private static AttributeReferenceEvaluationException pathFailure(AttributeSelectorKey ref, PathEvaluationException e)
	{
		return e.getStatus().getStatusCode().isSyntaxError()
				? AttributeReferenceEvaluationException.forSyntaxError(ref, e)
				: AttributeReferenceEvaluationException.forResolverFailure(ref, e);
	}

	@Override
	public Optional<Entity> getEntity(CategoryId id){
		return request.getEntity(id);
	}

}
