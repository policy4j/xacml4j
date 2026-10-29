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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

final class TargetIndex<T extends DecisionRule>
{
	private static final String STRING_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	private static final Class<?> BUILT_IN_SPEC = FunctionProvider.builder().withDefaultFunctions().build()
			.getFunction(STRING_EQUAL).orElseThrow().getClass();
	private static final StringVal PROBE = StringVal.of("xacml4j-index-probe");
	private static final BagOfValues SAME = XacmlTypes.STRING.bagOf(PROBE);
	private static final BagOfValues DIFFERENT = XacmlTypes.STRING.bagOf(StringVal.of("xacml4j-index-other"));
	// Bound the copies of always-selected children retained for each literal.
	private static final int MAX_CACHED_ANY = 8;

	private final List<T> children;
	private final Optional<AttributeDesignatorKey> key;
	private final BitSet any;
	private final List<T> anyChildren;
	private final Map<String, List<Integer>> positions;
	private final Map<String, List<T>> singleValueCandidates;
	private final Optional<RangeTargetIndex<T>> ranges;

	private TargetIndex(List<T> children){
		this.children = children;
		this.key = Optional.empty();
		this.any = new BitSet();
		this.anyChildren = children;
		this.positions = Map.of();
		this.singleValueCandidates = Map.of();
		this.ranges = Optional.empty();
	}

	private TargetIndex(List<T> children, RangeTargetIndex<T> ranges){
		this.children = children;
		this.key = Optional.empty();
		this.any = new BitSet();
		this.anyChildren = children;
		this.positions = Map.of();
		this.singleValueCandidates = Map.of();
		this.ranges = Optional.of(ranges);
	}

	private TargetIndex(List<T> children, Optional<AttributeDesignatorKey> key,
			BitSet any, Map<String, List<Integer>> positions){
		this.children = children;
		this.key = key;
		this.any = (BitSet)any.clone();
		this.anyChildren = candidatesFor(this.any);
		Map<String, List<Integer>> immutable = new HashMap<>();
		positions.forEach((value, indexes) -> immutable.put(value, List.copyOf(indexes)));
		this.positions = Map.copyOf(immutable);
		this.singleValueCandidates = new ConcurrentHashMap<>();
		this.ranges = Optional.empty();
	}

	List<T> candidates(EvaluationContext context){
		if(ranges.isPresent()) return ranges.orElseThrow().candidates(context);
		if(key.isEmpty() || context.isValidateFuncParamsAtRuntime()) return children;
		AttributeDesignatorKey ref = key.orElseThrow();
		Optional<BagOfValues> values;
		try{
			Optional<Entity> entity = context.resolveEntity(ref.getCategory());
			if(entity.isEmpty()) return children;
			values = entity.orElseThrow().resolve(ref);
		}catch(RuntimeException | Error failure){
			return children;
		}
		if(values.isEmpty()) return children;
		BagOfValues bag = values.orElseThrow();
		if(bag.isEmpty()) return anyChildren;
		if(bag.size() == 1){
			Value<?> value = bag.valueAt(0);
			if(!(value instanceof StringVal string)) return children;
			List<Integer> indexes = positions.get(string.get());
			if(indexes == null) return anyChildren;
			if(anyChildren.size() > MAX_CACHED_ANY) return candidatesFor(indexes);
			List<T> cached = singleValueCandidates.get(string.get());
			return cached != null ? cached : singleValueCandidates.computeIfAbsent(string.get(), ignored -> candidatesFor(indexes));
		}
		BitSet selected = new BitSet(children.size());
		selected.or(any);
		for(int i = 0; i < bag.size(); i++){
			Value<?> value = bag.valueAt(i);
			if(!(value instanceof StringVal string)) return children;
			List<Integer> found = positions.get(string.get());
			if(found != null){
				for(int j = 0; j < found.size(); j++) selected.set(found.get(j));
			}
		}
		return candidatesFor(selected);
	}

	private List<T> candidatesFor(List<Integer> indexes){
		BitSet selected = new BitSet(children.size());
		selected.or(any);
		for(int i = 0; i < indexes.size(); i++) selected.set(indexes.get(i));
		return candidatesFor(selected);
	}

	private List<T> candidatesFor(BitSet selected){
		return candidatesFor(children, selected);
	}

	static <T> List<T> candidatesFor(List<T> children, BitSet selected){
		int size = selected.cardinality();
		if(size == children.size()) return children;
		ImmutableList.Builder<T> result = ImmutableList.builderWithExpectedSize(size);
		for(int i = selected.nextSetBit(0); i >= 0; i = selected.nextSetBit(i + 1)){
			result.add(children.get(i));
		}
		return result.build();
	}

	static <T extends DecisionRule> TargetIndex<T> create(EvaluationContext context, List<T> children){
		if(children.size() < 2) return new TargetIndex<>(children);
		Builder<T> builder = new Builder<>(context);
		for(T child : children) builder.add(child);
		return builder.build(children);
	}

	static TargetIndex<Rule> createRules(EvaluationContext context, List<Rule> rules){
		TargetIndex<Rule> index = create(context, rules);
		if(rules.size() < 2 || index.key.isPresent()) return index;
		return RangeTargetIndex.create(context, rules)
				.map(ranges -> new TargetIndex<>(rules, ranges)).orElse(index);
	}

	static final class Builder<T extends DecisionRule>
	{
		private final EvaluationContext context;
		private final Map<FunctionSpec, Boolean> predicates = new IdentityHashMap<>();
		private final BitSet any = new BitSet();
		private final Map<String, List<Integer>> positions = new HashMap<>();
		private Optional<AttributeDesignatorKey> key = Optional.empty();
		private int size;

		Builder(EvaluationContext context){
			this.context = context;
		}

		void add(T child){
			int position = size++;
			DecisionRule target = child instanceof PinnedReference pin ? pin.target() : child;
			boolean known = target.getClass() == Policy.class || target.getClass() == PolicySet.class
					|| target.getClass() == Rule.class;
			Optional<IndexedTarget> indexed = known && target instanceof BaseDecisionRule rule
					? inspect(rule.getTarget()) : Optional.empty();
			if(indexed.isEmpty() || (key.isPresent() && !key.orElseThrow().equals(indexed.orElseThrow().key()))){
				any.set(position);
				return;
			}
			IndexedTarget entry = indexed.orElseThrow();
			if(key.isEmpty()) key = Optional.of(entry.key());
			for(String value : entry.values()){
				positions.computeIfAbsent(value, ignored -> new ArrayList<>()).add(position);
			}
		}

		TargetIndex<T> build(List<T> children){
			if(children.size() < 2 || key.isEmpty()) return new TargetIndex<>(children);
			return new TargetIndex<>(children, key, any, positions);
		}

		private Optional<IndexedTarget> inspect(Target target){
			if(target == null || target.getClass() != Target.class || target.getAnyOf().isEmpty()) return Optional.empty();
			MatchAnyOf anyOf = target.getAnyOf().iterator().next();
			if(anyOf.getClass() != MatchAnyOf.class) return Optional.empty();
			Optional<AttributeDesignatorKey> targetKey = Optional.empty();
			List<String> values = new ArrayList<>();
			for(MatchAllOf allOf : anyOf.getAllOf()){
				if(allOf.getClass() != MatchAllOf.class || allOf.getMatch().isEmpty()) return Optional.empty();
				Match match = allOf.getMatch().iterator().next();
				if(match.getClass() != Match.class || match.getReference().getClass() != AttributeDesignator.class
						|| !(match.getAttributeValue() instanceof StringVal literal)
						|| match.predicate().getClass() != BUILT_IN_SPEC
						|| !STRING_EQUAL.equals(match.getMatchId())) return Optional.empty();
				AttributeDesignator designator = (AttributeDesignator)match.getReference();
				AttributeDesignatorKey ref = designator.getReferenceKey();
				if(designator.isMustBePresent() || ref.getCategory() == null || !XacmlTypes.STRING.equals(ref.getDataType())
						|| (targetKey.isPresent() && !targetKey.orElseThrow().equals(ref))
						|| !predicates.computeIfAbsent(match.predicate(), this::isStringEqual)) return Optional.empty();
				targetKey = Optional.of(ref);
				values.add(literal.get());
			}
			return targetKey.map(ref -> new IndexedTarget(ref, values));
		}

		private boolean isStringEqual(FunctionSpec predicate){
			if(predicate.getClass() != BUILT_IN_SPEC) return false;
			return predicate.scan(context, PROBE, SAME) == BagStorage.Scan.MATCH
					&& predicate.scan(context, PROBE, DIFFERENT) == BagStorage.Scan.NO_MATCH;
		}
	}

	private record IndexedTarget(AttributeDesignatorKey key, List<String> values) { }
}
