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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;

/** Prepared policy sets and reference resolutions for one PDP. */
public final class PreparedPolicySets
{
	private final boolean targetIndex;
	private volatile Generation current;

	private PreparedPolicySets(boolean targetIndex){
		this.targetIndex = targetIndex;
		this.current = new Generation(targetIndex);
	}

	/** Creates a registry, optionally enabling the restricted target index. */
	public static PreparedPolicySets create(boolean targetIndex){
		return new PreparedPolicySets(targetIndex);
	}

	/** Retires the current generation for subsequent decisions. */
	public void invalidate(){
		current = new Generation(targetIndex);
	}

	Generation generation(){
		return current;
	}
}

final class Generation
{
	final ConcurrentMap<CompositeDecisionRuleIDReference, CompositeDecisionRule> targets = new ConcurrentHashMap<>();
	final ConcurrentMap<PolicySet, PreparedList> sets = new MapMaker().weakKeys().makeMap();
	final Optional<Indexes> indexes;
	/** This generation as the decisions that hold it give it, wrapped once instead of once per decision (cycle AU) */
	final Optional<Generation> held = Optional.of(this);

	Generation(boolean targetIndex){
		indexes = targetIndex ? Optional.of(new Indexes()) : Optional.empty();
	}
}

final class Indexes
{
	final ConcurrentMap<PolicySet, TargetIndex<CompositeDecisionRule>> sets = new MapMaker().weakKeys().makeMap();
	final ConcurrentMap<Policy, TargetIndex<Rule>> policies = new MapMaker().weakKeys().makeMap();
}

record PreparedList(List<CompositeDecisionRule> children)
{
	PreparedList {
		children = ImmutableList.copyOf(children);
	}
}
