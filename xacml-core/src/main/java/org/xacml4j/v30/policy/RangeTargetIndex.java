package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4j.org
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
import java.util.Arrays;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

/** Ordered candidates for the first AnyOf of inline rule targets (design D46). */
final class RangeTargetIndex<T extends DecisionRule>
{
	private static final String FUNCTION = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final Class<?> BUILT_IN_SPEC = FunctionProvider.builder().withDefaultFunctions().build()
			.getFunction(FUNCTION + "integer-less-than-or-equal").orElseThrow().getClass();
	private static final IntegerVal INTEGER_PROBE = IntegerVal.of(0);
	private static final BagOfValues INTEGER_BEFORE = XacmlTypes.INTEGER.bagOf(IntegerVal.of(-1));
	private static final BagOfValues INTEGER_EQUAL = XacmlTypes.INTEGER.bagOf(INTEGER_PROBE);
	private static final BagOfValues INTEGER_AFTER = XacmlTypes.INTEGER.bagOf(IntegerVal.of(1));
	private static final ISO8601Date DATE_PROBE = ISO8601Date.of("2024-06-15Z");
	private static final BagOfValues DATE_BEFORE = XacmlTypes.DATE.bagOf(ISO8601Date.of("2024-06-14Z"));
	private static final BagOfValues DATE_EQUAL = XacmlTypes.DATE.bagOf(DATE_PROBE);
	private static final BagOfValues DATE_AFTER = XacmlTypes.DATE.bagOf(ISO8601Date.of("2024-06-16Z"));

	private final List<T> children;
	private final AttributeDesignatorKey key;
	private final BitSet any;
	private final int[] childForAlternative;
	private final Endpoint[] lower;
	private final Endpoint[] upper;

	private RangeTargetIndex(List<T> children, AttributeDesignatorKey key, BitSet any,
			List<Integer> childForAlternative, List<Endpoint> lower, List<Endpoint> upper){
		this.children = children;
		this.key = key;
		this.any = (BitSet)any.clone();
		this.childForAlternative = childForAlternative.stream().mapToInt(Integer::intValue).toArray();
		this.lower = lower.toArray(Endpoint[]::new);
		this.upper = upper.toArray(Endpoint[]::new);
		Arrays.sort(this.lower, (a, b) -> compare(a.value(), b.value()));
		Arrays.sort(this.upper, (a, b) -> compare(a.value(), b.value()));
	}

	static <T extends DecisionRule> Optional<RangeTargetIndex<T>> create(EvaluationContext context, List<T> children){
		try{
			Builder<T> builder = new Builder<>(context);
			for(int i = 0; i < children.size(); i++) builder.add(children.get(i), i);
			return builder.build(children);
		}catch(RuntimeException | Error failure){
			return Optional.empty();
		}
	}

	List<T> candidates(EvaluationContext context){
		try{
			EvaluationContext top = DescendantEvaluationContext.topOf(context);
			if(!(top instanceof RootEvaluationContext root) || !root.isDecidingThread()
					|| !root.hasRequestAttributeResolution() || root.getDecisionObservation().isEnabled()
					|| context.isValidateFuncParamsAtRuntime() || context.getCurrentPolicy() == null
					|| context.getCurrentRule() != null) return children;
			Optional<Entity> entity = context.resolveEntity(key.getCategory());
			if(entity.isEmpty()) return children;
			Optional<BagOfValues> values = entity.orElseThrow().resolve(key);
			if(values.isEmpty() || values.orElseThrow().isEmpty()) return children;
			BagOfValues bag = values.orElseThrow();
			if(bag.getBagValueType() != key.getDataType()) return children;
			Value<?> min = bag.valueAt(0);
			if(!isValue(key, min)) return children;
			Value<?> max = min;
			for(int i = 1; i < bag.size(); i++){
				Value<?> value = bag.valueAt(i);
				if(!isValue(key, value)) return children;
				if(compare(value, min) < 0) min = value;
				if(compare(value, max) > 0) max = value;
			}
			BitSet alternatives = new BitSet(childForAlternative.length);
			alternatives.set(0, childForAlternative.length);
			for(int i = upperBound(lower, max); i < lower.length; i++) alternatives.clear(lower[i].alternative());
			for(int i = 0, end = upperBound(upper, min); i < end; i++) alternatives.clear(upper[i].alternative());
			BitSet selected = new BitSet(children.size());
			selected.or(any);
			for(int i = alternatives.nextSetBit(0); i >= 0; i = alternatives.nextSetBit(i + 1)){
				selected.set(childForAlternative[i]);
			}
			return TargetIndex.candidatesFor(children, selected);
		}catch(RuntimeException | Error failure){
			return children;
		}
	}

	private static int upperBound(Endpoint[] endpoints, Value<?> value){
		int low = 0;
		int high = endpoints.length;
		while(low < high){
			int mid = (low + high) >>> 1;
			if(compare(endpoints[mid].value(), value) <= 0) low = mid + 1;
			else high = mid;
		}
		return low;
	}

	private static boolean isValue(AttributeDesignatorKey key, Value<?> value){
		return key.getDataType() == XacmlTypes.INTEGER && value.getClass() == IntegerVal.class
				|| key.getDataType() == XacmlTypes.DATE && value.getClass() == ISO8601Date.class;
	}

	private static int compare(Value<?> left, Value<?> right){
		if(left.getClass() == IntegerVal.class){
			return Long.compare(((IntegerVal)left).get(), ((IntegerVal)right).get());
		}
		return ((ISO8601Date)left).compareTo((ISO8601Date)right);
	}

	private record Endpoint(Value<?> value, int alternative) { }
	private record Bound(Value<?> value, boolean lower) { }
	private record IndexedTarget(AttributeDesignatorKey key, List<List<Bound>> alternatives) { }

	private static final class Builder<T extends DecisionRule>
	{
		private final EvaluationContext context;
		private final Map<FunctionSpec, Boolean> predicates = new IdentityHashMap<>();
		private final BitSet any = new BitSet();
		private final List<Integer> children = new ArrayList<>();
		private final List<Endpoint> lower = new ArrayList<>();
		private final List<Endpoint> upper = new ArrayList<>();
		private Optional<AttributeDesignatorKey> key = Optional.empty();

		private Builder(EvaluationContext context){
			this.context = context;
		}

		private void add(T child, int position){
			Optional<IndexedTarget> target = child.getClass() == Rule.class
					? inspect(((Rule)child).getTarget()) : Optional.empty();
			if(target.isEmpty() || key.isPresent() && !key.orElseThrow().equals(target.orElseThrow().key())){
				any.set(position);
				return;
			}
			IndexedTarget entry = target.orElseThrow();
			if(key.isEmpty()) key = Optional.of(entry.key());
			for(List<Bound> bounds : entry.alternatives()){
				int alternative = children.size();
				children.add(position);
				for(Bound bound : bounds){
					(bound.lower() ? lower : upper).add(new Endpoint(bound.value(), alternative));
				}
			}
		}

		private Optional<RangeTargetIndex<T>> build(List<T> rules){
			return key.map(ref -> new RangeTargetIndex<>(rules, ref, any, children, lower, upper));
		}

		private Optional<IndexedTarget> inspect(Target target){
			if(target == null || target.getClass() != Target.class || target.getAnyOf().isEmpty()) return Optional.empty();
			MatchAnyOf first = target.getAnyOf().iterator().next();
			if(first.getClass() != MatchAnyOf.class || first.getAllOf().isEmpty()) return Optional.empty();
			Optional<AttributeDesignatorKey> targetKey = Optional.empty();
			List<List<Bound>> alternatives = new ArrayList<>();
			for(MatchAllOf all : first.getAllOf()){
				if(all.getClass() != MatchAllOf.class || all.getMatch().isEmpty()) return Optional.empty();
				List<Bound> bounds = new ArrayList<>();
				for(Match match : all.getMatch()){
					if(match.getClass() != Match.class || match.getReference().getClass() != AttributeDesignator.class)
						return Optional.empty();
					AttributeDesignator designator = (AttributeDesignator)match.getReference();
					AttributeDesignatorKey ref = designator.getReferenceKey();
					Value<?> literal = match.getAttributeValue();
					if(designator.isMustBePresent() || ref.getCategory() == null || !isValue(ref, literal)
							|| literal.getEvaluatesTo() != ref.getDataType()
							|| targetKey.isPresent() && !targetKey.orElseThrow().equals(ref)) return Optional.empty();
					FunctionSpec predicate = match.predicate();
					if(predicate.getClass() != BUILT_IN_SPEC || !predicate.hasDirectScan(literal)) return Optional.empty();
					String prefix = FUNCTION + (ref.getDataType() == XacmlTypes.INTEGER ? "integer" : "date");
					boolean isLower = (prefix + "-less-than-or-equal").equals(predicate.getId());
					if(!isLower && !(prefix + "-greater-than").equals(predicate.getId())) return Optional.empty();
					if(!predicates.computeIfAbsent(predicate, ignored -> certified(predicate, ref, isLower))) return Optional.empty();
					targetKey = Optional.of(ref);
					bounds.add(new Bound(literal, isLower));
				}
				alternatives.add(bounds);
			}
			return targetKey.map(ref -> new IndexedTarget(ref, alternatives));
		}

		private boolean certified(FunctionSpec predicate, AttributeDesignatorKey ref, boolean lower){
			boolean integer = ref.getDataType() == XacmlTypes.INTEGER;
			Value<?> literal = integer ? INTEGER_PROBE : DATE_PROBE;
			BagOfValues before = integer ? INTEGER_BEFORE : DATE_BEFORE;
			BagOfValues equal = integer ? INTEGER_EQUAL : DATE_EQUAL;
			BagOfValues after = integer ? INTEGER_AFTER : DATE_AFTER;
			return predicate.scan(context, literal, before) == (lower ? BagStorage.Scan.NO_MATCH : BagStorage.Scan.MATCH)
					&& predicate.scan(context, literal, equal) == (lower ? BagStorage.Scan.MATCH : BagStorage.Scan.NO_MATCH)
					&& predicate.scan(context, literal, after) == (lower ? BagStorage.Scan.MATCH : BagStorage.Scan.NO_MATCH);
		}
	}
}
