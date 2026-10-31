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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.xacml4j.v30.Expression;
import org.xacml4j.v30.types.Value;

/**
 * Tells whether evaluating a target, a condition, or an expression can write advice or obligations
 * to the evaluation context (design D36). The engine's own expressions, matches, and variable
 * definitions, with functions that write no responses, cannot. A class of another implementation, or
 * another function, counts as one that can. An attribute resolver writes, if at all, to the root
 * context, which no staging holds, so the designators and selectors that call it count as silent.
 */
final class ResponseWriters
{
	/** Exact engine definitions already visited in this walk; allocated at the first reference. */
	private Set<VariableDefinition> variables;

	private ResponseWriters(){
	}

	static boolean canWrite(Target target){
		if(target == null){
			return false;
		}
		if(target.getClass() != Target.class){
			return true;
		}
		ResponseWriters walk = new ResponseWriters();
		for(MatchAnyOf anyOf : target.getAnyOf()){
			for(MatchAllOf allOf : anyOf.getAllOf()){
				for(Match match : allOf.getMatch()){
					if(walk.writes(match)){
						return true;
					}
				}
			}
		}
		return false;
	}

	static boolean canWrite(Condition condition){
		return condition != null
				&& (condition.getClass() != Condition.class || canWrite(condition.getExpression()));
	}

	static boolean canWrite(Expression expression){
		return new ResponseWriters().writes(expression);
	}

	private boolean writes(Expression expression){
		if(expression == null
				|| expression instanceof AttributeDesignator
				|| expression instanceof AttributeSelector){
			return false;
		}
		if(expression instanceof Apply apply){
			if(!apply.function().writesNoResponses()){
				return true;
			}
			for(Expression argument : apply.getArguments()){
				if(writes(argument)){
					return true;
				}
			}
			return false;
		}
		if(expression instanceof FunctionReference reference){
			return !reference.function().writesNoResponses();
		}
		if(expression instanceof VariableReference reference && reference.getClass() == VariableReference.class){
			// A definition of another class can evaluate otherwise than by its expression
			VariableDefinition definition = reference.getDefinition();
			if(definition.getClass() != VariableDefinition.class){
				return true;
			}
			if(variables == null){
				variables = Collections.newSetFromMap(new IdentityHashMap<>());
			}
			// A response-writing path ends the whole walk, so revisits need no further scan.
			return variables.add(definition) && writes(definition.getExpression());
		}
		if(expression instanceof Value<?>){
			return expression.getClass().getPackage() != Value.class.getPackage();
		}
		return true;
	}

	private boolean writes(Match match){
		return !match.predicate().writesNoResponses()
				|| writes(match.getReference())
				|| writes(match.getAttributeValue());
	}
}
