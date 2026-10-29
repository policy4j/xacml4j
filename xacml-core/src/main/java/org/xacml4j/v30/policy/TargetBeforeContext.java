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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Optional;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.types.AnyURI;
import org.xacml4j.v30.types.Base64Binary;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.DNSName;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.HexBinary;
import org.xacml4j.v30.types.IPAddress;
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.ISO8601DateTime;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.xacml4j.v30.types.ISO8601Time;
import org.xacml4j.v30.types.ISO8601YearMonthDuration;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.RFC822Name;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.X500Name;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

/**
 * The target of a child, matched before the child has a context (change optimize-pdp-evaluation,
 * design D41). A target of built-in matches over request designators is decided from the request
 * entity alone, with the bag and the scan that the match itself would use: a child that cannot
 * match gets no context, and a child that matches is not matched again. Only an unobserved
 * decision of a handler that reads designators from the request entity takes part, and every
 * uncertain input, an absent entity or bag, a scan without an answer, or a failure, leaves the
 * child to its own evaluation.
 */
public final class TargetBeforeContext
{
	static final Optional<MatchResult> MATCHED = Optional.of(MatchResult.MATCH);
	static final Optional<MatchResult> NOT_MATCHED = Optional.of(MatchResult.NOMATCH);
	private static final Optional<MatchResult> UNKNOWN = Optional.empty();

	private TargetBeforeContext(){ }

	/**
	 * Evaluates a child in a context of its own, except that a child whose target cannot match gets
	 * none, and a child whose target matches is not matched again
	 */
	public static Decision evaluateIfMatch(EvaluationContext context, DecisionRule child){
		Optional<MatchResult> known = precheck(context, child);
		if(known.isEmpty()) return child.evaluate(child.createContext(context));
		return root(context).evaluatePrechecked(context, child, known.get());
	}

	/**
	 * Tells whether a child's target cannot match, for an algorithm that matches every child before
	 * it evaluates one: the child answers its match call from the parent context and is skipped. A
	 * child whose target matches answers its next match call, in the context the algorithm creates,
	 * from the same check; any other child is matched as before.
	 */
	public static boolean isDefinitelyNoMatch(EvaluationContext context, DecisionRule child){
		Optional<MatchResult> known = precheck(context, child);
		return known.isPresent() && root(context).skipPrechecked(context, child, known.get());
	}

	/** The precheck's answer for a rule about to match its target, taken once */
	static Optional<MatchResult> takePrechecked(EvaluationContext context, DecisionRule rule){
		EvaluationContext top = DescendantEvaluationContext.topOf(context);
		return top instanceof RootEvaluationContext root ? root.takePrechecked(rule) : UNKNOWN;
	}

	private static RootEvaluationContext root(EvaluationContext context){
		return (RootEvaluationContext)DescendantEvaluationContext.topOf(context);
	}

	/**
	 * The plan of a target that the request entity can decide: every match a built-in predicate
	 * with a bag scan, over a designator of a built-in type and a literal of that type; or the
	 * empty plan for any other target
	 */
	static Plan prepare(Target target){
		if(target == null || target.getClass() != Target.class || target.getAnyOf().isEmpty()) return Plan.EMPTY;
		ImmutableList.Builder<List<List<Match>>> anyOfs = ImmutableList.builder();
		for(MatchAnyOf any : target.getAnyOf()){
			if(any.getClass() != MatchAnyOf.class || any.getAllOf().isEmpty()) return Plan.EMPTY;
			ImmutableList.Builder<List<Match>> allOfs = ImmutableList.builder();
			for(MatchAllOf all : any.getAllOf()){
				if(all.getClass() != MatchAllOf.class || all.getMatch().isEmpty()) return Plan.EMPTY;
				ImmutableList.Builder<Match> matches = ImmutableList.builder();
				for(Match match : all.getMatch()){
					if(match.getClass() != Match.class || match.getReference().getClass() != AttributeDesignator.class
							|| !safeValues(match) || !match.predicate().hasDirectScan(match.getAttributeValue())) return Plan.EMPTY;
					matches.add(match);
				}
				allOfs.add(matches.build());
			}
			anyOfs.add(allOfs.build());
		}
		return new Plan(anyOfs.build());
	}

	static final class Plan
	{
		private static final Plan EMPTY = new Plan(List.of());
		private final List<List<List<Match>>> anyOfs;

		private Plan(List<List<List<Match>>> anyOfs){
			this.anyOfs = anyOfs;
		}
	}

	/** Whether the designator has a built-in type and the literal is that type's own value */
	private static boolean safeValues(Match match){
		Value<?> literal = match.getAttributeValue();
		ValueType type = ((AttributeDesignator)match.getReference()).getReferenceKey().getDataType();
		if(!(type instanceof XacmlTypes builtIn)) return false;
		Class<?> valueClass = literal.getClass();
		boolean known = switch(builtIn){
			case STRING -> valueClass == StringVal.class;
			case INTEGER -> valueClass == IntegerVal.class;
			case DOUBLE -> valueClass == DoubleVal.class;
			case BOOLEAN -> valueClass == BooleanVal.class;
			case ANYURI -> valueClass == AnyURI.class;
			case BASE64BINARY -> valueClass == Base64Binary.class;
			case HEXBINARY -> valueClass == HexBinary.class;
			case DATE -> valueClass == ISO8601Date.class;
			case DATETIME -> valueClass == ISO8601DateTime.class;
			case TIME -> valueClass == ISO8601Time.class;
			case DAYTIMEDURATION -> valueClass == ISO8601DayTimeDuration.class;
			case YEARMONTHDURATION -> valueClass == ISO8601YearMonthDuration.class;
			case DNSNAME -> valueClass == DNSName.class;
			case IPADDRESS -> valueClass == IPAddress.class;
			case RFC822NAME -> valueClass == RFC822Name.class;
			case X500NAME -> valueClass == X500Name.class;
			default -> false;
		};
		return known && literal.getEvaluatesTo() == type;
	}

	/**
	 * The target of a child decided from the request entity, by the chain of a target: an AnyOf
	 * with no AllOf whose matches all match decides a no-match, and all AnyOfs matched decide a
	 * match; or nothing, when the child is left to its own evaluation
	 */
	private static Optional<MatchResult> precheck(EvaluationContext context, DecisionRule child){
		DecisionRule resolved = child instanceof PinnedReference pin ? pin.target() : child;
		if(resolved.getClass() != Rule.class && resolved.getClass() != Policy.class && resolved.getClass() != PolicySet.class){
			return UNKNOWN;
		}
		Plan plan = ((BaseDecisionRule)resolved).contextFreeTarget;
		// An instance made without its constructor, as a test double is, has no plan
		if(plan == null || plan.anyOfs.isEmpty()) return UNKNOWN;
		List<List<List<Match>>> anyOfs = plan.anyOfs;
		try{
			EvaluationContext top = DescendantEvaluationContext.topOf(context);
			if(!(top instanceof RootEvaluationContext root) || !root.isDecidingThread() || !root.hasRequestAttributeResolution()
					|| root.getDecisionObservation().isEnabled() || context.isValidateFuncParamsAtRuntime()) return UNKNOWN;
			if(child instanceof PinnedReference pin){
				if(!pin.canCheckTarget(context)) return UNKNOWN;
			}else if(resolved instanceof Rule){
				if(context.getCurrentPolicy() == null || context.getCurrentRule() == resolved) return UNKNOWN;
			}else if(resolved instanceof Policy){
				if(context.getCurrentPolicy() != null) return UNKNOWN;
			}else if(context.getCurrentPolicy() != null || context.getCurrentPolicySet() == resolved){
				return UNKNOWN;
			}
			for(int i = 0; i < anyOfs.size(); i++){
				List<List<Match>> allOfs = anyOfs.get(i);
				boolean anyMatches = false;
				for(int j = 0; j < allOfs.size() && !anyMatches; j++){
					List<Match> matches = allOfs.get(j);
					boolean allMatch = true;
					for(int k = 0; k < matches.size() && allMatch; k++){
						Match match = matches.get(k);
						AttributeDesignatorKey key = ((AttributeDesignator)match.getReference()).getReferenceKey();
						Optional<Entity> entity = context.resolveEntity(key.getCategory());
						if(entity.isEmpty()) return UNKNOWN;
						Optional<BagOfValues> bag = entity.get().resolve(key);
						if(bag.isEmpty()) return UNKNOWN;
						BagStorage.Scan scan = match.predicate().scan(context, match.getAttributeValue(), bag.get());
						if(scan == BagStorage.Scan.NONE) return UNKNOWN;
						allMatch = scan == BagStorage.Scan.MATCH;
					}
					anyMatches = allMatch;
				}
				if(!anyMatches) return NOT_MATCHED;
			}
			return MATCHED;
		}catch(RuntimeException failure){
			return UNKNOWN;
		}
	}
}
