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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;


public class Target implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(Target.class);

	private final Collection<MatchAnyOf> matches;

	/**
	 * Creates target from a given {@link Builder} instance
	 *
	 * @param b a {@link Builder} instance
	 */
	public Target(Builder b){
		this.matches = b.allAnyOf.build();
	}

	public Collection<MatchAnyOf> getAnyOf(){
		return matches;
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Evaluates a target against given {@link EvaluationContext}.
	 * During target evaluation {@link AttributeDesignator} or
	 * {@link AttributeSelector} are resolved only from request
	 * context
	 *
	 * @param context an evaluation context
	 * @return {@link MatchResult}
	 */
	public MatchResult match(EvaluationContext context)
	{
		MatchResult state = MatchResult.MATCH;
		for(Matchable m : matches){
			MatchResult r = m.match(context);
			if(r == MatchResult.NOMATCH){
				state = r;
				log.debug("Found AnyOf with match result=\"{}\", " +
						"no need to evaluate target further", r);
				break;
			}
			if(r == MatchResult.INDETERMINATE){
				state = r;
				log.debug("Found AnyOf with match result=\"{}\", " +
						"no need to evaluate target further", r);
				break;
			}
		}
		if(log.isDebugEnabled()){
			log.debug("Target " +
					"match state=\"{}\"", state);
		}
		return state;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(Matchable m : matches){
			m.accept(v);
		}
		v.visitLeave(this);
	}

	@Override
	public int hashCode(){
		return matches.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return false;
		}
		if(!(o instanceof Target)){
			return false;
		}

		Target t = (Target)o;
		return matches.equals(t.matches);
	}

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("AnyOf", matches)
				.toString();
	}


	public static class Builder
	{
		private ImmutableList.Builder<MatchAnyOf> allAnyOf = ImmutableList.builder();

		private Builder(){
		}

		public Builder anyOf(MatchAnyOf ...anyOfs){
			allAnyOf.add(anyOfs);
			return this;
		}

		/**
		 * Adds {@link MatchAnyOf} created from a given
		 * array of {@link MatchAnyOf} instances
		 * @param anyOfs an array of {@code MatchAnyOf} matches
		 */
		public Builder anyOf(Iterable<MatchAnyOf> anyOfs){
			allAnyOf.addAll(anyOfs);
			return this;
		}

		public Target build(){
			return new Target(this);
		}
	}
}
