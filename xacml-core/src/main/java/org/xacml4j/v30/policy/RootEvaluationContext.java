package org.xacml4j.v30.policy;

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
import java.time.Clock;
import java.time.Duration;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.spi.observe.DecisionObservation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.xacml4j.v30.types.Entity;


public final class RootEvaluationContext implements EvaluationContext
{
	// The state below is created on its first write and published with a compare-and-set
	// (change optimize-pdp-evaluation, D9); a getter reads it without creating it (design D32).
	// The advice and obligations of the decision wait in one holder of four immutable lists,
	// each replaced with a compare-and-set (cycle AU); the views of the getters follow the
	// lists, so a view kept at any time sees later writes and allows their removal, as the
	// values views of the maps before them did (reviews rv006 to rv010, R-YZ). Missing state
	// compares and hashes as empty.
	private static final VarHandle RESPONSES = handle(RootEvaluationContext.class, "responses", Responses.class);
	private static final VarHandle EVALUATED = handle(RootEvaluationContext.class, "evaluated", Evaluated.class);
	private static final VarHandle EVALUATION_STATUS_MAP = handle(RootEvaluationContext.class, "evaluationStatusMap", ConcurrentMap.class);
	private static final VarHandle SNAPSHOTS = handle(RootEvaluationContext.class, "snapshots", ConcurrentMap.class);
	private static final VarHandle COMBINED_DECISION_CACHE_TTL = handle(RootEvaluationContext.class, "combinedDecisionCacheTTL", Duration.class);
	/** The order of the policy identifier list (design D32) */
	private static final Comparator<CompositeDecisionRule> BY_ID = Comparator.comparing(CompositeDecisionRule::getId);
	private static final CompositeDecisionRule[] NO_POLICIES = new CompositeDecisionRule[0];

	private final Configuration configuration;
	private final EvaluationContextHandler contextHandler;
	private final Optional<Generation> generation;
	/** The thread that created this context and decides on it; the precheck slot is confined to it (design D41) */
	private final Thread decidingThread = Thread.currentThread();
	/** The answer of the target precheck for one child, taken by the child's next match call (design D41) */
	private DecisionRule prechecked;
	/** Whether the target of the prechecked child matches; any other answer is taken as no match */
	private boolean precheckedMatch;
	/**
	 * The context of the innermost composite rule that stages its responses now. The responses wait
	 * in a frame per context, created on the first of them (design D36, cycle AN, review R-AN)
	 */
	private CompositeRuleEvaluationContext staging;
	/**
	 * The composite rule contexts that stage now, recorded as each opened its staging (review R-AN2):
	 * bit i is the i-th composite rule context on the chain of the innermost, which is bit 0
	 */
	private long stagingScopes;
	private Staged staged;
	/** The reference misses and the shared expression values of the decision, apart by key type (designs D25, D45) */
	private volatile ConcurrentMap<Object, Object> snapshots;
	/** The advice and obligations of the decision, created on the first of them (cycle AU) */
	private volatile Responses responses;
	/** The applicable policies, the one recorded last first (design D32, cycle AU) */
	private volatile Evaluated evaluated;
	private volatile ConcurrentMap<DecisionRule, Status> evaluationStatusMap;

	private boolean validateFuncParamsAtRuntime;
	private Status evaluationStatus;
	/** The shortest time to live set for the decision, updated with a compare-and-set (cycle AH) */
	private volatile Duration combinedDecisionCacheTTL;
	/** Whether the applicable policies are recorded for the policy identifier list (design D32) */
	private final boolean recordEvaluatedPolicies;
	/** The observation of the decision; not part of equality (design D34) */
	private final DecisionObservation observation;

	/** The settings shared by the individual decisions of one PDP. */
	public static final class Configuration
	{
		private final Clock clock;
		private final Duration defaultDecisionCacheTTL;
		private final XPathVersion defaultXPathVersion;
		private final PolicyReferenceResolver resolver;

		public Configuration(Clock clock, Duration defaultDecisionCacheTTL,
				XPathVersion defaultXPathVersion, PolicyReferenceResolver resolver) {
			this.clock = Preconditions.checkNotNull(clock);
			this.defaultDecisionCacheTTL = defaultDecisionCacheTTL;
			this.defaultXPathVersion = defaultXPathVersion;
			this.resolver = Preconditions.checkNotNull(resolver);
		}
	}

	public RootEvaluationContext(
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler) {
		this(Clock.systemUTC(), validateFuncParamsAtRuntime,
				defaultDecisionCacheTTL,
				defaultXPathVersion,
				referenceResolver,
				contextHandler);
	}

	public RootEvaluationContext(
			Clock clock,
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler) {
		this(clock, validateFuncParamsAtRuntime, defaultDecisionCacheTTL, defaultXPathVersion,
				referenceResolver, contextHandler, true);
	}

	/**
	 * @param recordEvaluatedPolicies whether the applicable policies are recorded for
	 * {@link #getEvaluatedPolicies()}; a decision whose request does not ask for the policy
	 * identifier list has no use for them (design D32)
	 */
	public RootEvaluationContext(
			Clock clock,
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler,
			boolean recordEvaluatedPolicies) {
		this(clock, validateFuncParamsAtRuntime, defaultDecisionCacheTTL, defaultXPathVersion,
				referenceResolver, contextHandler, recordEvaluatedPolicies, Optional.empty(), DecisionObservation.none());
	}

	/** Creates a context whose decision the given observation observes (design D34). */
	public RootEvaluationContext(
			Clock clock,
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler,
			boolean recordEvaluatedPolicies,
			DecisionObservation observation) {
		this(clock, validateFuncParamsAtRuntime, defaultDecisionCacheTTL, defaultXPathVersion,
				referenceResolver, contextHandler, recordEvaluatedPolicies, Optional.empty(), observation);
	}

	/** Creates a context that holds one generation for its decision. */
	public RootEvaluationContext(
			Clock clock,
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler,
			boolean recordEvaluatedPolicies,
			PreparedPolicySets prepared) {
		this(clock, validateFuncParamsAtRuntime, defaultDecisionCacheTTL, defaultXPathVersion,
				referenceResolver, contextHandler, recordEvaluatedPolicies, prepared.generation().held,
				DecisionObservation.none());
	}

	/** Creates a context that holds one generation for its decision, which the given observation observes. */
	public RootEvaluationContext(
			Clock clock,
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler,
			boolean recordEvaluatedPolicies,
			PreparedPolicySets prepared,
			DecisionObservation observation) {
		this(clock, validateFuncParamsAtRuntime, defaultDecisionCacheTTL, defaultXPathVersion,
				referenceResolver, contextHandler, recordEvaluatedPolicies, prepared.generation().held,
				observation);
	}

	/**
	 * @param generation the generation of the prepared policy sets the decision holds, if any, for
	 * {@link #generation()}: wrapped once per generation (cycles AH, AU)
	 */
	private RootEvaluationContext(
			Clock clock,
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			XPathVersion defaultXPathVersion,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler,
			boolean recordEvaluatedPolicies,
			Optional<Generation> generation,
			DecisionObservation observation) {
		this(new Configuration(clock, defaultDecisionCacheTTL, defaultXPathVersion, referenceResolver),
				validateFuncParamsAtRuntime, contextHandler, recordEvaluatedPolicies, generation, observation);
	}

	/** Creates a decision context using settings shared by its PDP. */
	public RootEvaluationContext(Configuration configuration, boolean validateFuncParamsAtRuntime,
			EvaluationContextHandler contextHandler, boolean recordEvaluatedPolicies,
			DecisionObservation observation) {
		this(configuration, validateFuncParamsAtRuntime, contextHandler, recordEvaluatedPolicies,
				Optional.empty(), observation);
	}

	/** Creates a decision context using shared settings and holding one prepared generation. */
	public RootEvaluationContext(Configuration configuration, boolean validateFuncParamsAtRuntime,
			EvaluationContextHandler contextHandler, boolean recordEvaluatedPolicies,
			PreparedPolicySets prepared, DecisionObservation observation) {
		this(configuration, validateFuncParamsAtRuntime, contextHandler, recordEvaluatedPolicies,
				prepared.generation().held, observation);
	}

	private RootEvaluationContext(Configuration configuration, boolean validateFuncParamsAtRuntime,
			EvaluationContextHandler contextHandler, boolean recordEvaluatedPolicies,
			Optional<Generation> generation, DecisionObservation observation) {
		Preconditions.checkNotNull(contextHandler);
		this.configuration = Preconditions.checkNotNull(configuration);
		this.observation = Preconditions.checkNotNull(observation);
		this.contextHandler = contextHandler;
		this.generation = Preconditions.checkNotNull(generation);
		this.combinedDecisionCacheTTL = configuration.defaultDecisionCacheTTL;
		this.validateFuncParamsAtRuntime = validateFuncParamsAtRuntime;
		this.recordEvaluatedPolicies = recordEvaluatedPolicies;
	}

	private static VarHandle handle(Class<?> owner, String field, Class<?> type) {
		try {
			return MethodHandles.lookup().findVarHandle(owner, field, type);
		} catch (ReflectiveOperationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	/** @return the given new value, or the value another thread set first */
	private <T> T created(VarHandle field, T value) {
		@SuppressWarnings("unchecked")
		T witness = (T) field.compareAndExchange(this, null, value);
		return witness != null ? witness : value;
	}

	private Responses createdResponses() {
		Responses r = responses;
		return r != null ? r : created(RESPONSES, new Responses());
	}

	private ConcurrentMap<DecisionRule, Status> evaluationStatusMap() {
		ConcurrentMap<DecisionRule, Status> m = evaluationStatusMap;
		return m != null ? m : created(EVALUATION_STATUS_MAP, new ConcurrentHashMap<>());
	}

	public RootEvaluationContext(
			boolean validateFuncParamsAtRuntime,
			Duration defaultDecisionCacheTTL,
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler handler){
		this(validateFuncParamsAtRuntime,
				defaultDecisionCacheTTL,
				XPathVersion.XPATH1,
				referenceResolver,
				handler);
	}

	@Override
	public void setEvaluationStatus(Status status) {
		this.evaluationStatus = status;
	}

	@Override
	public void setEvaluationStatus(DecisionRule rule, Status status) {
		if(rule == null){
			this.evaluationStatus = status;
			return;
		}
		evaluationStatusMap().put(rule, status);
	}

	@Override
	public void setEvaluationStatusIfAbsent(java.util.function.Supplier<Status> supplier)
	{
		if(evaluationStatus == null){
			Status status = supplier.get();
			this.evaluationStatus = status;
		}
	}

	@Override
	public Optional<Status> getEvaluationStatus(DecisionRule rule) {
		// The concurrent map rejected a null rule; so does the missing one
		java.util.Objects.requireNonNull(rule);
		Map<DecisionRule, Status> m = evaluationStatusMap;
		return m == null ? Optional.empty() : Optional.ofNullable(m.get(rule));
	}

	@Override
	public XPathVersion getXPathVersion() {
		return configuration.defaultXPathVersion;
	}

	@Override
	public Clock getClock(){
		return configuration.clock;
	}

	/** Never: an extended Indeterminate pass starts in the context of a rule below the root */
	@Override
	public boolean isExtendedIndeterminateEval() {
		return false;
	}

	@Override
	public EvaluationContext createExtIndeterminateEvalContext() {
		return this;
	}

	@Override
	public Optional<Status> getEvaluationStatus() {
		return Optional.ofNullable(evaluationStatus);
	}


	@Override
	public Duration getDecisionCacheTTL() {
		Duration v = combinedDecisionCacheTTL;
		return v != null?v:Duration.ZERO;
	}

	/** Keeps the shorter of the time to live set so far and the given one, as the other fields are set (cycle AH) */
	@Override
	public void setDecisionCacheTTL(Duration ttl) {
		Duration current;
		Duration shorter;
		do {
			current = combinedDecisionCacheTTL;
			shorter = Duration.ofMillis(Math.min(current.toMillis(), ttl.toMillis()));
		} while (!COMBINED_DECISION_CACHE_TTL.compareAndSet(this, current, shorter));
	}

	@Override
	public final void addEvaluationResult(CompositeDecisionRule policy, Decision result) {
		if (recordEvaluatedPolicies && !result.isIndeterminate() && result != Decision.NOT_APPLICABLE) {
			// The map by id rejected a policy without an id when it was recorded
			java.util.Objects.requireNonNull(policy.getId());
			Evaluated last;
			do {
				last = evaluated;
			} while (!EVALUATED.compareAndSet(this, last, new Evaluated(policy, last)));
		}
	}

	/** Whether the applicable policies are recorded, for the policy identifier list (design D32) */
	boolean recordsEvaluatedPolicies() {
		return recordEvaluatedPolicies;
	}

	@Override
	public boolean isValidateFuncParamsAtRuntime() {
		return validateFuncParamsAtRuntime;
	}

	@Override
	public void setValidateFuncParamsAtRuntime(boolean validate){
		this.validateFuncParamsAtRuntime = validate;
	}

	@Override
	public void addAdvices(Decision d, Iterable<Advice> advices)
	{
		java.util.Objects.requireNonNull(d, "decision");
		if(d == Decision.PERMIT){
			add(Responses.PERMIT_ADVICES, advices);
		}else if(d == Decision.DENY){
			add(Responses.DENY_ADVICES, advices);
		}
	}

	@Override
	public Optional<Entity> resolveEntity(CategoryId categoryId) {
		return contextHandler.getEntity(categoryId);
	}

	@Override
	public void addObligations(Decision d, Iterable<Obligation> obligations)
	{
		java.util.Objects.requireNonNull(d, "decision");
		if(d == Decision.PERMIT){
			add(Responses.PERMIT_OBLIGATIONS, obligations);
		}else if(d == Decision.DENY){
			add(Responses.DENY_OBLIGATIONS, obligations);
		}
	}

	/**
	 * Merges responses into one of the lists of the decision; only an immutable list is kept as
	 * given, as a caller can change any other later (cycle AU)
	 */
	private <T extends DecisionRuleResponse> void add(VarHandle list, Iterable<T> responses)
	{
		ImmutableList<T> added = ImmutableList.copyOf(responses);
		if(added.isEmpty()){
			return;
		}
		Responses r = createdResponses();
		ImmutableList<T> current;
		ImmutableList<T> merged;
		do{
			current = Responses.get(list, r);
			merged = merged(current, added);
		}while(!list.compareAndSet(r, current, merged));
	}

	/**
	 * A list with responses merged in by id (XACML 3.0, section 7.18): a response whose id is in the
	 * list merges into the response there, in its place; the others follow in their order. Added to
	 * an empty list, responses with distinct ids are the list (cycle AU)
	 */
	private static <T extends DecisionRuleResponse> ImmutableList<T> merged(ImmutableList<T> list, ImmutableList<T> added)
	{
		if(list.isEmpty() && hasDistinctIds(added)){
			return added;
		}
		List<T> merged = new ArrayList<>(list.size() + added.size());
		merged.addAll(list);
		for(int i = 0; i < added.size(); i++){
			T response = added.get(i);
			int at = indexOf(merged, response.getId());
			if(at < 0){
				merged.add(response);
			}else{
				merged.set(at, merged.get(at).merge(response));
			}
		}
		return ImmutableList.copyOf(merged);
	}

	private static boolean hasDistinctIds(List<? extends DecisionRuleResponse> responses)
	{
		for(int i = 1; i < responses.size(); i++){
			String id = responses.get(i).getId();
			for(int j = 0; j < i; j++){
				if(responses.get(j).getId().equals(id)){
					return false;
				}
			}
		}
		return true;
	}

	private static int indexOf(List<? extends DecisionRuleResponse> responses, String id)
	{
		for(int i = 0; i < responses.size(); i++){
			if(responses.get(i).getId().equals(id)){
				return i;
			}
		}
		return -1;
	}

	@Override
	public Optional<BagOfValues> resolve(AttributeReferenceKey ref)
			throws EvaluationException {
		return contextHandler.resolve(this, ref);
	}

	/**
	 * Implementation always return {@code null}
	 */
	@Override
	public EvaluationContext getParentContext() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public Policy getCurrentPolicy() {
		return null;
	}

	@Override
	public DecisionRule getCurrentRule() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public PolicyIDReference getCurrentPolicyIDReference() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public PolicySetIDReference getCurrentPolicySetIDReference() {
		return null;
	}

	/**
	 * Implementation always returns {@code null}
	 */
	@Override
	public PolicySet getCurrentPolicySet() {
		return null;
	}

	@Override
	public final ValueExp getVariableEvaluationResult(
			String variableId){
		return null;
	}

	@Override
	public final void setVariableEvaluationResult(String variableId, ValueExp value) {
	}

	@Override
	public final CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
			throws PolicyResolutionException
	{
		if(generation.isPresent()){
			return replay(snapshot(ref, generation.orElseThrow()));
		}
		if(ref instanceof PolicyIDReference){
			return resolveEntity((PolicyIDReference)ref);
		}
		if(ref instanceof PolicySetIDReference){
			return resolveEntity((PolicySetIDReference)ref);
		}
		throw new PolicyResolutionException(this);
	}

	Optional<Generation> generation(){
		return generation;
	}

	boolean hasRequestAttributeResolution(){
		return contextHandler.usesRequestEntityForDesignators();
	}

	/** Whether the current thread is the one that decides on this context */
	boolean isDecidingThread(){
		return Thread.currentThread() == decidingThread;
	}

	/**
	 * Takes the precheck's answer for a rule, the prechecked child or the target of its pin, on the
	 * deciding thread; a rule's first match call does, so a target decided from the request entity
	 * is matched once.
	 */
	Optional<MatchResult> takePrechecked(DecisionRule rule){
		if(!isDecidingThread()) return Optional.empty();
		DecisionRule child = prechecked;
		if(child != rule && !(child instanceof PinnedReference pin && pin.target() == rule)) return Optional.empty();
		boolean match = precheckedMatch;
		clearPrechecked();
		return match ? TargetBeforeContext.MATCHED : TargetBeforeContext.NOT_MATCHED;
	}

	/**
	 * Evaluates a child whose target the precheck decided: a child that cannot match answers its
	 * match call from this parent context and is NotApplicable without a context of its own; a child
	 * that matches gets its context, and its first match call answers from the precheck.
	 */
	Decision evaluatePrechecked(EvaluationContext context, DecisionRule child, MatchResult result){
		leave(child, result);
		try{
			if(result == MatchResult.NOMATCH){
				child.isMatch(context);
				return Decision.NOT_APPLICABLE;
			}
			return child.evaluate(child.createContext(context));
		}finally{
			clearPrechecked();
		}
	}

	/**
	 * For an algorithm that matches every child before it evaluates one: a child that cannot match
	 * answers its match call from this parent context now and is skipped; a child that matches
	 * answers its next match call, in the context the algorithm creates.
	 */
	boolean skipPrechecked(EvaluationContext context, DecisionRule child, MatchResult result){
		leave(child, result);
		if(result != MatchResult.NOMATCH) return false;
		try{
			child.isMatch(context);
			return true;
		}finally{
			clearPrechecked();
		}
	}

	private void leave(DecisionRule child, MatchResult result){
		prechecked = child;
		precheckedMatch = result == MatchResult.MATCH;
	}

	private void clearPrechecked(){
		prechecked = null;
		precheckedMatch = false;
	}

	/**
	 * Whether a context stages now (design D36, review R-AN2): it is the innermost staging context, or
	 * an enclosing one recorded as staging when it opened, whatever thread writes through it
	 */
	boolean isStaging(CompositeRuleEvaluationContext owner){
		EvaluationContext c = staging;
		if(owner == c){
			return true;
		}
		long scopes = stagingScopes >>> 1;
		while(scopes != 0 && c instanceof DescendantEvaluationContext d){
			c = d.getParent();
			if(c == owner){
				return (scopes & 1) != 0;
			}
			if(c instanceof CompositeRuleEvaluationContext){
				scopes >>>= 1;
			}
		}
		return false;
	}

	/**
	 * Makes a context the innermost staging one, recording the staging contexts it is in (review
	 * R-AN2). It must be below the innermost staging context, if any, within the span of the record;
	 * otherwise nothing changes, and the answer is false
	 */
	boolean openStaging(CompositeRuleEvaluationContext owner){
		CompositeRuleEvaluationContext inner = staging;
		if(inner == null){
			staging = owner;
			stagingScopes = 1;
			return true;
		}
		int distance = 0;
		for(EvaluationContext c = owner; c != inner; ){
			if(!(c instanceof DescendantEvaluationContext d)){
				return false;
			}
			if(c instanceof CompositeRuleEvaluationContext){
				distance++;
			}
			c = d.getParent();
		}
		if(distance == 0 || distance > Long.numberOfLeadingZeros(stagingScopes)){
			return false;
		}
		stagingScopes = stagingScopes << distance | 1;
		staging = owner;
		return true;
	}

	/**
	 * Ends the staging of the innermost context: the staging context recorded before it is the
	 * innermost again. A call for another context does nothing
	 */
	void closeStaging(CompositeRuleEvaluationContext owner){
		if(staging != owner){
			return;
		}
		EvaluationContext c = owner;
		long scopes = stagingScopes >>> 1;
		while(scopes != 0 && c instanceof DescendantEvaluationContext d){
			c = d.getParent();
			if(c instanceof CompositeRuleEvaluationContext enclosing){
				if((scopes & 1) != 0){
					staging = enclosing;
					stagingScopes = scopes;
					return;
				}
				scopes >>>= 1;
			}
		}
		staging = null;
		stagingScopes = 0;
	}

	/**
	 * Stages advice that passed through the context of a composite rule while that rule stages
	 * (design D36, review R-AN): it waits in the frame of that context, created on the first
	 * response, until the rule publishes or drops the frame
	 */
	void stageAdvices(CompositeRuleEvaluationContext owner, Decision d, Iterable<Advice> advices){
		frame(owner).addAdvices(d, advices);
	}

	void stageObligations(CompositeRuleEvaluationContext owner, Decision d, Iterable<Obligation> obligations){
		frame(owner).addObligations(d, obligations);
	}

	/**
	 * Publishes the responses of the frame of a rule that match its decision through its context,
	 * which stages no more and forwards them to the staging of the rule that encloses it or to
	 * the decision
	 */
	void publishStaged(CompositeRuleEvaluationContext owner, Decision d){
		Staged frame = takeFrame(owner);
		if(frame != null){
			frame.publishTo(owner, d);
		}
	}

	/** Drops the frame of a rule, with what it still holds */
	void dropStaged(CompositeRuleEvaluationContext owner){
		takeFrame(owner);
	}

	/** The frame of a context, created on the first response staged in it */
	private Staged frame(CompositeRuleEvaluationContext owner){
		for(Staged frame = staged; frame != null; frame = frame.next){
			if(frame.owner == owner){
				return frame;
			}
		}
		return staged = new Staged(owner, staged);
	}

	/** Removes and returns the frame of a context, {@code null} when it staged nothing */
	private Staged takeFrame(CompositeRuleEvaluationContext owner){
		Staged before = null;
		for(Staged frame = staged; frame != null; before = frame, frame = frame.next){
			if(frame.owner != owner){
				continue;
			}
			if(before == null){
				staged = frame.next;
			}else{
				before.next = frame.next;
			}
			return frame;
		}
		return null;
	}

	/**
	 * The responses staged for one composite rule until its decision is known (designs D19, D36):
	 * an immutable list per decision, the first list staged for it as it is (cycle AU)
	 */
	static final class Staged
	{
		/** The context of the rule whose responses wait here; none for the frame of a wrapper context */
		private final CompositeRuleEvaluationContext owner;
		/** The frame of the rule that was staging before this one */
		private Staged next;
		private ImmutableList<Advice> permitAdvices = ImmutableList.of();
		private ImmutableList<Advice> denyAdvices = ImmutableList.of();
		private ImmutableList<Obligation> permitObligations = ImmutableList.of();
		private ImmutableList<Obligation> denyObligations = ImmutableList.of();

		/** A frame on its own, as a wrapper context holds one */
		Staged(){
			this(null, null);
		}

		private Staged(CompositeRuleEvaluationContext owner, Staged next){
			this.owner = owner;
			this.next = next;
		}

		void addAdvices(Decision d, Iterable<Advice> advices){
			if(d == Decision.PERMIT){
				permitAdvices = staged(permitAdvices, advices);
			}else if(d == Decision.DENY){
				denyAdvices = staged(denyAdvices, advices);
			}
		}

		void addObligations(Decision d, Iterable<Obligation> obligations){
			if(d == Decision.PERMIT){
				permitObligations = staged(permitObligations, obligations);
			}else if(d == Decision.DENY){
				denyObligations = staged(denyObligations, obligations);
			}
		}

		/** Gives the responses that match a Permit or Deny decision to a context */
		void publishTo(EvaluationContext context, Decision d){
			ImmutableList<Advice> advices = (d == Decision.PERMIT)?permitAdvices:denyAdvices;
			if(!advices.isEmpty()){
				context.addAdvices(d, advices);
			}
			ImmutableList<Obligation> obligations = (d == Decision.PERMIT)?permitObligations:denyObligations;
			if(!obligations.isEmpty()){
				context.addObligations(d, obligations);
			}
		}

		/**
		 * Adds responses to a staging list: the first immutable list is kept as it is, as a rule
		 * gives it; any other is copied, as a caller can change it later; a later list is joined
		 * at the exact size (cycle AU)
		 */
		private static <T> ImmutableList<T> staged(ImmutableList<T> staged, Iterable<T> responses){
			ImmutableList<T> added = ImmutableList.copyOf(responses);
			if(staged.isEmpty() || added.isEmpty()){
				return staged.isEmpty() ? added : staged;
			}
			return ImmutableList.<T>builderWithExpectedSize(staged.size() + added.size())
					.addAll(staged)
					.addAll(added)
					.build();
		}
	}

	@Override
	public DecisionObservation getDecisionObservation(){
		return observation;
	}

	/**
	 * Gets the observation of the decision that a context belongs to (design D34).
	 *
	 * @param context an evaluation context
	 * @return the {@link DecisionObservation} of the decision, or {@link DecisionObservation#none()}
	 */
	public static DecisionObservation observationOf(EvaluationContext context){
		EvaluationContext top = DescendantEvaluationContext.topOf(context);
		if(top instanceof RootEvaluationContext root) return root.observation;
		if(top == null) return DecisionObservation.none();
		DecisionObservation observation = top.getDecisionObservation();
		return observation != null ? observation : DecisionObservation.none();
	}

	/** Called on the deciding thread only. */
	boolean isMissed(CompositeDecisionRuleIDReference ref){
		ConcurrentMap<Object, Object> m = snapshots;
		return m != null && m.containsKey(ref);
	}

	/** Whether a reference missed in this decision: one look-up of the marker, whatever else the snapshots hold (design D45) */
	boolean hasMisses(){
		ConcurrentMap<Object, Object> m = snapshots;
		return m != null && m.containsKey(ReferenceMiss.INSTANCE);
	}

	/**
	 * The value of a shared condition expression in this decision (design D45), read without creating
	 * the snapshots. The snapshots keep the value in its optional, so a read creates none. Called on the
	 * deciding thread only.
	 */
	@SuppressWarnings("unchecked")
	Optional<ValueExp> sharedValue(PreparedConditions.Canonical expression){
		ConcurrentMap<Object, Object> m = snapshots;
		return m == null ? Optional.empty() : (Optional<ValueExp>)m.getOrDefault(expression, Optional.empty());
	}

	/**
	 * Publishes the successful value of a shared condition expression (design D45): the value published
	 * first is the one that every occurrence gets. Called on the deciding thread only.
	 */
	@SuppressWarnings("unchecked")
	ValueExp share(PreparedConditions.Canonical expression, ValueExp value){
		ConcurrentMap<Object, Object> m = snapshots;
		if(m == null) m = created(SNAPSHOTS, new ConcurrentHashMap<>());
		Object previous = m.putIfAbsent(expression, Optional.of(value));
		return previous == null ? value : ((Optional<ValueExp>)previous).orElseThrow();
	}

	Optional<CompositeDecisionRule> outcome(CompositeDecisionRuleIDReference ref){
		try{
			return Optional.of(resolve(ref)).filter(ref::isReferenceTo);
		}catch(RuntimeException | Error failure){
			return Optional.empty();
		}
	}

	/** Called on the deciding thread only. */
	private Object snapshot(CompositeDecisionRuleIDReference ref, Generation current){
		ConcurrentMap<Object, Object> m = snapshots;
		Object previous = m == null ? null : m.get(ref);
		if(previous != null) return previous;
		CompositeDecisionRule target = current.targets.get(ref);
		if(target != null) return target;
		try{
			CompositeDecisionRule resolved;
			if(ref instanceof PolicyIDReference policy){
				resolved = configuration.resolver.resolve(policy);
			}else if(ref instanceof PolicySetIDReference set){
				resolved = configuration.resolver.resolve(set);
			}else{
				throw new PolicyResolutionException(this);
			}
			if(resolved == null) return remember(ref, Missing.INSTANCE);
			if(!ref.isReferenceTo(resolved)) return remember(ref, resolved);
			CompositeDecisionRule winner = current.targets.putIfAbsent(ref, resolved);
			return winner == null ? resolved : winner;
		}catch(RuntimeException | Error failure){
			return remember(ref, failure);
		}
	}

	private Object remember(CompositeDecisionRuleIDReference ref, Object outcome){
		ConcurrentMap<Object, Object> m = snapshots;
		if(m == null) m = created(SNAPSHOTS, new ConcurrentHashMap<>());
		// The marker before the outcome, so that a recorded miss is always found by the marker (design D45)
		m.putIfAbsent(ReferenceMiss.INSTANCE, ReferenceMiss.INSTANCE);
		Object previous = m.putIfAbsent(ref, outcome);
		return previous == null ? outcome : previous;
	}

	private CompositeDecisionRule replay(Object outcome){
		if(outcome instanceof CompositeDecisionRule target) return target;
		if(outcome instanceof RuntimeException failure) throw failure;
		if(outcome instanceof Error failure) throw failure;
		throw new PolicyResolutionException(this);
	}

	private enum Missing { INSTANCE }

	/** The key that marks a decision in which a reference missed; no reference or expression equals it (design D45) */
	private enum ReferenceMiss { INSTANCE }

	private Policy resolveEntity(PolicyIDReference ref)
		throws PolicyResolutionException {
		Policy p =	configuration.resolver.resolve(ref);
		if(p == null){
			throw new PolicyResolutionException(this);
		}
		return p;
	}

	private PolicySet resolveEntity(PolicySetIDReference ref)
			throws PolicyResolutionException {
		PolicySet p = configuration.resolver.resolve(ref);
		if(p == null){
			throw new PolicyResolutionException(this);
		}
		return p;
	}

	@Override
	public Collection<CompositeDecisionRuleIDReference> getEvaluatedPolicies() {
		// A new modifiable list, as the stream collector returned (review RVC2)
		CompositeDecisionRule[] policies = applicablePolicies();
		List<CompositeDecisionRuleIDReference> references = new ArrayList<>(policies.length);
		for(CompositeDecisionRule policy : policies){
			references.add(policy.getReference());
		}
		return references;
	}

	/**
	 * The policy identifier list of the decision: the references of {@link #getEvaluatedPolicies()}
	 * as an immutable list of the exact size, for its result (cycle AU)
	 *
	 * @return the references of the applicable policies, in the order of their ids
	 */
	public ImmutableList<CompositeDecisionRuleIDReference> policyIdentifiers() {
		CompositeDecisionRule[] policies = applicablePolicies();
		if(policies.length == 0){
			return ImmutableList.of();
		}
		ImmutableList.Builder<CompositeDecisionRuleIDReference> references = ImmutableList.builderWithExpectedSize(policies.length);
		for(CompositeDecisionRule policy : policies){
			references.add(policy.getReference());
		}
		return references.build();
	}

	/**
	 * The applicable policies in the order of their ids, sorted once: one per id, the one recorded
	 * last, as the map by id that held them before kept it (design D32, cycle AU)
	 */
	private CompositeDecisionRule[] applicablePolicies() {
		Evaluated last = evaluated;
		int n = 0;
		for(Evaluated e = last; e != null; e = e.earlier()){
			n++;
		}
		if(n == 0){
			return NO_POLICIES;
		}
		CompositeDecisionRule[] policies = new CompositeDecisionRule[n];
		int i = 0;
		for(Evaluated e = last; e != null; e = e.earlier()){
			policies[i++] = e.policy();
		}
		// A stable sort keeps the one recorded last of an id first
		Arrays.sort(policies, BY_ID);
		int distinct = 0;
		for(CompositeDecisionRule policy : policies){
			if(distinct == 0 || !policies[distinct - 1].getId().equals(policy.getId())){
				policies[distinct++] = policy;
			}
		}
		return distinct == n ? policies : Arrays.copyOf(policies, distinct);
	}

	/** An applicable policy, in front of those recorded before it (cycle AU) */
	private record Evaluated(CompositeDecisionRule policy, Evaluated earlier) {}

	@Override
	public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators() {
		return contextHandler.getResolvedDesignators();
	}

	@Override
	public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors() {
		return contextHandler.getResolvedSelectors();
	}

	@Override
	public boolean hasResolvedDesignators() {
		return contextHandler.hasResolvedDesignators();
	}

	@Override
	public Collection<Obligation> getMatchingObligations(final Decision decision) {
		// Read without creating the lists; the view follows them (design D32, review R-YZ)
		return new Matching<>(decision == Decision.PERMIT ? Responses.PERMIT_OBLIGATIONS : Responses.DENY_OBLIGATIONS);
	}

	@Override
	public Collection<Advice> getMatchingAdvices(final Decision decision) {
		return new Matching<>(decision == Decision.PERMIT ? Responses.PERMIT_ADVICES : Responses.DENY_ADVICES);
	}

	/**
	 * The obligations of a Permit or Deny decision as they are now, one per id, in the order of the
	 * first write of each id: the list that {@link #getMatchingObligations(Decision)} views, for the
	 * result of the decision (cycle AU)
	 *
	 * @param decision an access decision
	 * @return the immutable list of the obligations
	 */
	public ImmutableList<Obligation> matchingObligations(Decision decision) {
		return now(decision == Decision.PERMIT ? Responses.PERMIT_OBLIGATIONS : Responses.DENY_OBLIGATIONS);
	}

	/**
	 * The advice of a Permit or Deny decision as they are now, as {@link #matchingObligations(Decision)}
	 *
	 * @param decision an access decision
	 * @return the immutable list of the advice
	 */
	public ImmutableList<Advice> matchingAdvices(Decision decision) {
		return now(decision == Decision.PERMIT ? Responses.PERMIT_ADVICES : Responses.DENY_ADVICES);
	}

	/** One of the lists of the responses as it is now; the empty list before the first response */
	private <T extends DecisionRuleResponse> ImmutableList<T> now(VarHandle list) {
		Responses r = responses;
		return r == null ? ImmutableList.of() : Responses.get(list, r);
	}

	@Override
	public String toString() {
		return MoreObjects
				.toStringHelper(this)
				.add("defaultXPathVersion", configuration.defaultXPathVersion)
				.add("contextHandler", contextHandler.getClass().getSimpleName())
				.add("resolver", configuration.resolver.getClass().getSimpleName())
				.add("clock", configuration.clock)
				.add("validateFuncParamsAtRuntime", validateFuncParamsAtRuntime)
				.add("evaluationStatus", evaluationStatus)
				.add("combinedDecisionCacheTTL", combinedDecisionCacheTTL)
				.add("extendedIndeterminateEval", false)
				.add("denyAdvices", byId(Responses.DENY_ADVICES))
				.add("denyObligations", byId(Responses.DENY_OBLIGATIONS))
				.add("permitAdvices", byId(Responses.PERMIT_ADVICES))
				.add("permitObligations", byId(Responses.PERMIT_OBLIGATIONS))
				.add("evaluatedPolicies", evaluatedPolicies())
				.toString();
	}

	@Override
	public int hashCode() {
		// The responses and the applicable policies hash as the maps by id that held them; missing
		// state hashes as an empty map: both give 0
		return Objects.hashCode(configuration.defaultXPathVersion,
				contextHandler,
				configuration.resolver,
				byId(Responses.DENY_ADVICES),
				byId(Responses.DENY_OBLIGATIONS),
				byId(Responses.PERMIT_ADVICES),
				byId(Responses.PERMIT_OBLIGATIONS),
				evaluatedPolicies(),
				validateFuncParamsAtRuntime,
				evaluationStatus,
				combinedDecisionCacheTTL,
				false);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof RootEvaluationContext)) {
			return false;
		}
		final RootEvaluationContext c = (RootEvaluationContext) o;
		return Objects.equal(configuration.defaultXPathVersion, c.configuration.defaultXPathVersion)
			&& Objects.equal(contextHandler, c.contextHandler)
			&& Objects.equal(configuration.resolver, c.configuration.resolver)
			&& byId(Responses.DENY_ADVICES).equals(c.byId(Responses.DENY_ADVICES))
			&& byId(Responses.DENY_OBLIGATIONS).equals(c.byId(Responses.DENY_OBLIGATIONS))
			&& byId(Responses.PERMIT_ADVICES).equals(c.byId(Responses.PERMIT_ADVICES))
			&& byId(Responses.PERMIT_OBLIGATIONS).equals(c.byId(Responses.PERMIT_OBLIGATIONS))
			&& evaluatedPolicies().equals(c.evaluatedPolicies())
			&& Objects.equal(configuration.clock, c.configuration.clock)
			&& Objects.equal(validateFuncParamsAtRuntime, c.validateFuncParamsAtRuntime)
			&& Objects.equal(evaluationStatus, c.evaluationStatus)
			&& Objects.equal(combinedDecisionCacheTTL, c.combinedDecisionCacheTTL);
	}

	/**
	 * Clears context state
	 */
	public void clear() {
		this.combinedDecisionCacheTTL = null;
		this.evaluated = null;
	}

	/** One of the lists of the responses as the map by id that held them before (cycle AU) */
	private Map<String, DecisionRuleResponse> byId(VarHandle list) {
		ImmutableList<DecisionRuleResponse> responses = now(list);
		return Maps.uniqueIndex(responses, DecisionRuleResponse::getId);
	}

	/** The applicable policies as the map by id that held them before (cycle AU) */
	private Map<String, CompositeDecisionRule> evaluatedPolicies() {
		return Maps.uniqueIndex(Arrays.asList(applicablePolicies()), CompositeDecisionRule::getId);
	}

	/**
	 * The advice and the obligations of a decision for Permit and for Deny: four immutable lists, each
	 * with one response per id in the order of the first write of the id, and each replaced with a
	 * compare-and-set. The root creates it on the first response (cycle AU)
	 */
	private static final class Responses
	{
		static final VarHandle PERMIT_ADVICES = handle(Responses.class, "permitAdvices", ImmutableList.class);
		static final VarHandle DENY_ADVICES = handle(Responses.class, "denyAdvices", ImmutableList.class);
		static final VarHandle PERMIT_OBLIGATIONS = handle(Responses.class, "permitObligations", ImmutableList.class);
		static final VarHandle DENY_OBLIGATIONS = handle(Responses.class, "denyObligations", ImmutableList.class);

		private volatile ImmutableList<Advice> permitAdvices = ImmutableList.of();
		private volatile ImmutableList<Advice> denyAdvices = ImmutableList.of();
		private volatile ImmutableList<Obligation> permitObligations = ImmutableList.of();
		private volatile ImmutableList<Obligation> denyObligations = ImmutableList.of();

		@SuppressWarnings("unchecked")
		static <T extends DecisionRuleResponse> ImmutableList<T> get(VarHandle list, Responses r) {
			return (ImmutableList<T>) list.getVolatile(r);
		}
	}

	/**
	 * One list of the responses of the decision, as it is at each call: the view sees later writes,
	 * as the values view of the map before it did, removes a response by its id with a
	 * compare-and-set, and adds nothing (reviews rv006 to rv010, R-YZ, cycle AU)
	 */
	private final class Matching<T extends DecisionRuleResponse> extends AbstractCollection<T>
	{
		private final VarHandle list;

		Matching(VarHandle list) {
			this.list = list;
		}

		/** The list as it is now */
		private ImmutableList<T> current() {
			return now(list);
		}

		@Override
		public int size() {
			return current().size();
		}

		@Override
		public boolean isEmpty() {
			return current().isEmpty();
		}

		/** No null, as in the values view of a concurrent map (review rv009) */
		@Override
		public boolean contains(Object o) {
			return current().contains(java.util.Objects.requireNonNull(o));
		}

		@Override
		public Object[] toArray() {
			return current().toArray();
		}

		@Override
		public <A> A[] toArray(A[] a) {
			return current().toArray(a);
		}

		/** Iterates the list as it is now; a removal through the iterator removes the id, whatever its response is then */
		@Override
		public Iterator<T> iterator() {
			ImmutableList<T> responses = current();
			return new Iterator<>() {
				private int next;
				private T last;

				@Override
				public boolean hasNext() {
					return next < responses.size();
				}

				@Override
				public T next() {
					if(!hasNext()){
						throw new NoSuchElementException();
					}
					last = responses.get(next++);
					return last;
				}

				@Override
				public void remove() {
					Preconditions.checkState(last != null);
					removeWithId(last.getId(), any -> true);
					last = null;
				}
			};
		}

		@Override
		public boolean remove(Object o) {
			if(o != null){
				for(T response : current()){
					if(o.equals(response)){
						removeWithId(response.getId(), any -> true);
						return true;
					}
				}
			}
			return false;
		}

		/** Removes a tested response only while it is still there: one merged in meanwhile stays (review rv010) */
		@Override
		public boolean removeIf(Predicate<? super T> filter) {
			java.util.Objects.requireNonNull(filter);
			boolean removed = false;
			for(T response : current()){
				if(filter.test(response) && removeWithId(response.getId(), response::equals)){
					removed = true;
				}
			}
			return removed;
		}

		@Override
		public void clear() {
			Responses r = responses;
			if(r != null){
				list.setVolatile(r, ImmutableList.of());
			}
		}

		/**
		 * Removes the response of an id if it is the one to remove
		 *
		 * @return whether a response was removed
		 */
		private boolean removeWithId(String id, Predicate<? super T> toRemove) {
			Responses r = responses;
			if(r == null){
				return false;
			}
			ImmutableList<T> current;
			ImmutableList<T> rest;
			do{
				current = Responses.get(list, r);
				int at = indexOf(current, id);
				if(at < 0 || !toRemove.test(current.get(at))){
					return false;
				}
				rest = ImmutableList.<T>builderWithExpectedSize(current.size() - 1)
						.addAll(current.subList(0, at))
						.addAll(current.subList(at + 1, current.size()))
						.build();
			}while(!list.compareAndSet(r, current, rest));
			return true;
		}
	}
}
