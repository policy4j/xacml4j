package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The conditions of one policy, prepared once and kept by the policy, to share within a decision the
 * values of the expensive expressions that its rules repeat (change optimize-pdp-evaluation, design D45).
 * They depend on the policy alone, and what they hold of it is a cycle that goes with it (review R-D45-sol).
 * <p>
 * An expression is eligible when it is a literal string, a literal bag of strings, a string designator,
 * or string-one-and-only, string-normalize-space or string-normalize-to-lower-case, as the engine builds
 * them, over an eligible expression. Structurally equal eligible expressions of all the rule conditions
 * of the policy are counted together; the largest with at least two string transformations and at least
 * four occurrences share their value, and no occurrence below one of them does. A condition that holds
 * one gets a predicate in which its occurrences are {@link Shared} and the engine's calls above them are
 * built again around them; every other expression stays as it is.
 * <p>
 * An occurrence evaluates its expression once per decision, on the deciding thread of an unobserved
 * decision without validation at runtime, when every attribute that it reads has one string in the
 * request entity, and gives only a successful value to the later ones. Otherwise it evaluates the
 * expression as written. A value of an information point is never shared.
 */
final class PreparedConditions
{
	private static final String NORMALIZE_SPACE = "urn:oasis:names:tc:xacml:1.0:function:string-normalize-space";
	private static final String NORMALIZE_TO_LOWER_CASE = "urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case";
	/** The fewest occurrences of a shared expression in the conditions of a policy */
	private static final int OCCURRENCES = 4;
	/** The fewest string transformations, each of which allocates a string, in a shared expression */
	private static final int TRANSFORMATIONS = 2;
	/** The conditions of a policy that shares nothing */
	private static final PreparedConditions NONE = new PreparedConditions(Map.of());

	/**
	 * The prepared predicate of each condition that shares a value, by the identity of the condition, in
	 * the optional that a decision gets
	 */
	private final Map<Condition, Optional<Expression>> predicates;

	private PreparedConditions(Map<Condition, Optional<Expression>> predicates){
		this.predicates = predicates;
	}

	/**
	 * Counts the occurrences in a predicate of the largest eligible expressions with at least two string
	 * transformations that the engine's own calls reach, for the flag of the policy. Only the classes of
	 * the expressions are tested; an expression of another implementation is not called.
	 *
	 * @param predicate the predicate of a condition
	 * @return the occurrences
	 */
	static int occurrences(Expression predicate){
		if(transformations(predicate) >= TRANSFORMATIONS){
			return 1;
		}
		if(!(predicate instanceof Apply apply) || !isCrossable(apply)){
			return 0;
		}
		int occurrences = 0;
		List<Expression> arguments = apply.getArguments();
		for(int i = 0; i < arguments.size(); i++){
			occurrences += occurrences(arguments.get(i));
		}
		return occurrences;
	}

	/**
	 * A cheap conservative scan for the flag of a policy: whether the conditions of its rules hold at
	 * least four occurrences of eligible expressions with two string transformations. Whether they are
	 * equal is left to the preparation.
	 *
	 * @param rules the rules of a policy
	 * @return {@code true} when a decision may prepare the conditions
	 */
	static boolean mayShare(List<Rule> rules){
		int occurrences = 0;
		for(int i = 0; i < rules.size() && occurrences < OCCURRENCES; i++){
			Condition condition = rules.get(i).getCondition();
			if(condition != null){
				occurrences += condition.shareableOccurrences();
			}
		}
		return occurrences >= OCCURRENCES;
	}

	/**
	 * The prepared predicate of a condition of a policy that may share values, in an unobserved decision
	 * of the engine's root with a generation, on its deciding thread, without validation at runtime, and
	 * with request attributes from the request entity; none otherwise, and the condition evaluates its
	 * own. The policy prepares its conditions at the first such evaluation.
	 *
	 * @param context the context of the rule
	 * @param policy the policy of the rule
	 * @param condition the condition of the rule
	 * @return the prepared predicate to evaluate, if any
	 */
	static Optional<Expression> predicate(EvaluationContext context, Policy policy, Condition condition){
		if(!(DescendantEvaluationContext.topOf(context) instanceof RootEvaluationContext root)
				|| root.generation().isEmpty() || !shares(root, context)){
			return Optional.empty();
		}
		return policy.preparedConditions().predicates.getOrDefault(condition, Optional.empty());
	}

	/** Whether a decision shares values in a context; each shared occurrence asks again */
	private static boolean shares(RootEvaluationContext root, EvaluationContext context){
		return root.isDecidingThread() && !root.getDecisionObservation().isEnabled()
				&& !context.isValidateFuncParamsAtRuntime() && root.hasRequestAttributeResolution();
	}

	/**
	 * Prepares the conditions of the rules of a policy together; nothing is evaluated and no attribute is
	 * resolved
	 */
	static PreparedConditions prepare(Policy policy){
		List<Condition> conditions = new ArrayList<>();
		for(Rule rule : policy.getRules()){
			Condition condition = rule.getCondition();
			// Only a condition of the class itself counts occurrences: its expression is the one it evaluates
			if(condition != null && condition.shareableOccurrences() > 0){
				conditions.add(condition);
			}
		}
		Preparation preparation = new Preparation();
		for(Condition condition : conditions){
			preparation.count(condition.getExpression());
		}
		Set<Canonical> selected = preparation.select(conditions);
		if(selected.isEmpty()){
			return NONE;
		}
		Map<Condition, Optional<Expression>> predicates = new IdentityHashMap<>();
		for(Condition condition : conditions){
			Expression predicate = preparation.rebuild(condition.getExpression(), selected);
			if(predicate != condition.getExpression()){
				predicates.put(condition, Optional.of(predicate));
			}
		}
		return predicates.isEmpty() ? NONE : new PreparedConditions(predicates);
	}

	/** The string transformations of an eligible expression, or -1 for any other expression */
	private static int transformations(Expression e){
		if(e.getClass() == StringVal.class){
			return 0;
		}
		if(e instanceof BagOfValues bag){
			return isStringBag(bag) ? 0 : -1;
		}
		if(e instanceof AttributeDesignator designator){
			return isStringDesignator(designator) ? 0 : -1;
		}
		if(e instanceof Apply apply && isShareable(apply)){
			int below = transformations(apply.getArguments().get(0));
			return below < 0 ? -1 : below + (isTransformation(apply) ? 1 : 0);
		}
		return -1;
	}

	/** A type-correct call of one of the shareable functions, as the engine builds them, with one argument */
	private static boolean isShareable(Apply apply){
		return apply.isTypeCorrect() && apply.getArguments().size() == 1
				&& FunctionSpecBuilder.isShareableBuiltin(apply.function());
	}

	private static boolean isTransformation(Apply apply){
		String function = apply.getFunctionId();
		return NORMALIZE_SPACE.equals(function) || NORMALIZE_TO_LOWER_CASE.equals(function);
	}

	/**
	 * A type-correct call of a function of the engine, which only evaluates its arguments: its engine
	 * validator, if any, reads their types, so a shared occurrence can stand for an argument
	 */
	private static boolean isCrossable(Apply apply){
		return apply.isTypeCorrect() && FunctionSpecBuilder.usesBuiltInInvocation(apply.function());
	}

	private static boolean isStringDesignator(AttributeDesignator designator){
		AttributeDesignatorKey key = designator.getReferenceKey();
		return designator.getClass() == AttributeDesignator.class && key.getCategory() != null
				&& XacmlTypes.STRING.equals(key.getDataType());
	}

	private static boolean isStringBag(BagOfValues bag){
		if(!XacmlTypes.STRING.equals(bag.getBagValueType())){
			return false;
		}
		for(int i = 0; i < bag.size(); i++){
			if(bag.valueAt(i).getClass() != StringVal.class){
				return false;
			}
		}
		return true;
	}

	/**
	 * An eligible expression as the preparation of a policy knows it: structurally equal ones have one,
	 * and that of a shared expression is the key of its value in the snapshots of a decision
	 */
	static final class Canonical
	{
		/** The string transformations in the expression */
		private final int transformations;
		/** The expressions it is made of, itself included */
		private final int size;
		/** The attributes that the expression reads */
		private final List<AttributeDesignatorKey> designators;

		private Canonical(int transformations, int size, List<AttributeDesignatorKey> designators){
			this.transformations = transformations;
			this.size = size;
			this.designators = designators;
		}

		/**
		 * Whether every attribute that the expression reads has one string in the request, as the request
		 * entity tells, so that it resolves to that bag in the whole decision; an information point is
		 * never asked
		 */
		boolean readsOneRequestString(EvaluationContext context){
			try{
				for(int i = 0; i < designators.size(); i++){
					AttributeDesignatorKey key = designators.get(i);
					Optional<Entity> entity = context.resolveEntity(key.getCategory());
					if(entity.isEmpty()){
						return false;
					}
					Optional<BagOfValues> values = entity.orElseThrow().resolve(key);
					if(values.isEmpty()){
						return false;
					}
					BagOfValues bag = values.orElseThrow();
					if(bag.size() != 1 || bag.valueAt(0).getClass() != StringVal.class){
						return false;
					}
				}
				return true;
			}catch(RuntimeException e){
				return false;
			}
		}
	}

	/** A literal string, by its typed value */
	private record Literal(StringVal value) { }

	/** A literal bag of strings, by its values in order, each as often as the bag holds it */
	private record BagLiteral(List<Value<?>> values) { }

	/** A designator, by its category, attribute id, data type and issuer, and MustBePresent */
	private record Designator(AttributeDesignatorKey key, boolean mustBePresent) { }

	/** A call, by the identity of its function and of its canonical argument: the equality of a function is its id */
	private record Call(FunctionSpec function, Canonical argument)
	{
		@Override
		public boolean equals(Object o){
			return o instanceof Call c && c.function == function && c.argument == argument;
		}

		@Override
		public int hashCode(){
			return 31 * System.identityHashCode(function) + System.identityHashCode(argument);
		}
	}

	/** The canonical expressions of one policy while it is prepared */
	private static final class Preparation
	{
		/** The canonical expression of each structural key */
		private final Map<Object, Canonical> byKey = new HashMap<>();
		/** The canonical expression of each expression object seen, none for one that is not eligible */
		private final Map<Expression, Optional<Canonical>> seen = new IdentityHashMap<>();
		/** The reachable occurrences of each canonical expression, nested ones included */
		private final Map<Canonical, Integer> counts = new HashMap<>();

		/** Counts the occurrences of the eligible expressions that the engine's calls of a predicate reach */
		void count(Expression e){
			canonical(e).ifPresent(c -> counts.merge(c, 1, Integer::sum));
			if(e instanceof Apply apply && isCrossable(apply)){
				for(Expression argument : apply.getArguments()){
					count(argument);
				}
			}
		}

		/**
		 * The canonical expressions to share, the larger considered first: each with at least two string
		 * transformations and at least four occurrences that no selected expression holds
		 */
		Set<Canonical> select(List<Condition> conditions){
			List<Canonical> candidates = new ArrayList<>();
			counts.forEach((c, n) -> {
				if(c.transformations >= TRANSFORMATIONS && n >= OCCURRENCES){
					candidates.add(c);
				}
			});
			candidates.sort(Comparator.comparingInt((Canonical c) -> c.size).reversed());
			Set<Canonical> selected = Collections.newSetFromMap(new IdentityHashMap<>());
			for(Canonical candidate : candidates){
				int occurrences = 0;
				for(Condition condition : conditions){
					occurrences += occurrences(condition.getExpression(), candidate, selected);
				}
				if(occurrences >= OCCURRENCES){
					selected.add(candidate);
				}
			}
			return selected;
		}

		/** The occurrences of a candidate that the engine's calls of a predicate reach, outside selected expressions */
		private int occurrences(Expression e, Canonical candidate, Set<Canonical> selected){
			Optional<Canonical> c = canonical(e);
			if(c.isPresent() && c.get() == candidate){
				return 1;
			}
			if(c.isPresent() && selected.contains(c.get())){
				return 0;
			}
			if(!(e instanceof Apply apply) || !isCrossable(apply)){
				return 0;
			}
			int occurrences = 0;
			for(Expression argument : apply.getArguments()){
				occurrences += occurrences(argument, candidate, selected);
			}
			return occurrences;
		}

		/**
		 * The predicate with each occurrence of a selected expression shared and the engine's calls above
		 * them built again around them, with the same functions, positions and other arguments; the same
		 * expression when it holds none
		 */
		Expression rebuild(Expression e, Set<Canonical> selected){
			Optional<Canonical> c = canonical(e);
			if(c.isPresent() && selected.contains(c.get())){
				return new Shared(e, c.get());
			}
			if(!(e instanceof Apply apply) || !isCrossable(apply)){
				return e;
			}
			List<Expression> arguments = apply.getArguments();
			List<Expression> rebuilt = null;
			for(int i = 0; i < arguments.size(); i++){
				Expression argument = arguments.get(i);
				Expression shared = rebuild(argument, selected);
				if(shared != argument){
					if(rebuilt == null){
						rebuilt = new ArrayList<>(arguments);
					}
					rebuilt.set(i, shared);
				}
			}
			if(rebuilt == null){
				return e;
			}
			// Built as any call, also binding a literal first argument (design D6 of add-regex-engine-spi);
			// a copy that is not type correct would not evaluate as the call does, so the call stays
			Apply copy = Apply.builder(apply.function()).params(rebuilt).build();
			return copy.isTypeCorrect() ? copy : e;
		}

		private Optional<Canonical> canonical(Expression e){
			Optional<Canonical> known = seen.get(e);
			if(known == null){
				known = intern(e);
				seen.put(e, known);
			}
			return known;
		}

		private Optional<Canonical> intern(Expression e){
			if(e.getClass() == StringVal.class){
				return Optional.of(canonical(new Literal((StringVal)e), 0, 1, List.of()));
			}
			if(e instanceof BagOfValues bag){
				if(!isStringBag(bag)){
					return Optional.empty();
				}
				List<Value<?>> values = new ArrayList<>(bag.size());
				for(int i = 0; i < bag.size(); i++){
					values.add(bag.valueAt(i));
				}
				return Optional.of(canonical(new BagLiteral(List.copyOf(values)), 0, 1, List.of()));
			}
			if(e instanceof AttributeDesignator designator){
				return isStringDesignator(designator)
						? Optional.of(canonical(new Designator(designator.getReferenceKey(), designator.isMustBePresent()),
								0, 1, List.of(designator.getReferenceKey())))
						: Optional.empty();
			}
			if(e instanceof Apply apply && isShareable(apply)){
				Optional<Canonical> argument = canonical(apply.getArguments().get(0));
				if(argument.isEmpty()){
					return Optional.empty();
				}
				Canonical a = argument.orElseThrow();
				return Optional.of(canonical(new Call(apply.function(), a),
						a.transformations + (isTransformation(apply) ? 1 : 0), a.size + 1, a.designators));
			}
			return Optional.empty();
		}

		private Canonical canonical(Object key, int transformations, int size, List<AttributeDesignatorKey> designators){
			return byKey.computeIfAbsent(key, k -> new Canonical(transformations, size, designators));
		}
	}

	/**
	 * An occurrence of a shared expression in a prepared predicate. It gives the value that the
	 * snapshots of the decision hold for its canonical expression; or, when every attribute that the
	 * expression reads has one string in the request, it evaluates the expression and publishes a
	 * successful value with a put-if-absent. A failure is thrown and nothing is kept, so a later
	 * occurrence evaluates again. On any thread but the deciding one, in an observed decision, or with
	 * validation at runtime, it evaluates the expression as written.
	 */
	static final class Shared implements Expression
	{
		private final Expression expression;
		private final Canonical canonical;

		private Shared(Expression expression, Canonical canonical){
			this.expression = expression;
			this.canonical = canonical;
		}

		@Override
		public ValueExpTypeInfo getEvaluatesTo(){
			return expression.getEvaluatesTo();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Expression> T evaluate(EvaluationContext context) throws EvaluationException {
			if(!(DescendantEvaluationContext.topOf(context) instanceof RootEvaluationContext root) || !shares(root, context)){
				return expression.evaluate(context);
			}
			Optional<ValueExp> shared = root.sharedValue(canonical);
			if(shared.isPresent()){
				return (T)shared.orElseThrow();
			}
			if(!canonical.readsOneRequestString(context)){
				return expression.evaluate(context);
			}
			return (T)root.share(canonical, expression.evaluate(context));
		}

		@Override
		public void accept(ExpressionVisitor v){
			expression.accept(v);
		}

		@Override
		public String toString(){
			return expression.toString();
		}
	}
}
