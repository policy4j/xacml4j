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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyElement;

import java.util.Collection;

public class MatchAllOf implements PolicyElement, Matchable
{
	private final static Logger log = LoggerFactory.getLogger(MatchAllOf.class);

	private final Collection<Match> matches;

	/**
	 * Constructs a {@link MatchAllOf} with
	 * a given builder
	 *
	 * @param b a {@link MatchAllOf} builder defaultProvider
	 */
	private MatchAllOf(Builder b){
		this.matches = b.matches.build();
		Preconditions.checkArgument(matches.size() >= 1);
	}

	public static Builder builder(){
		return new Builder();
	}

	public Collection<Match> getMatch(){
		return matches;
	}

	@Override
	public MatchResult match(EvaluationContext context)
	{
		MatchResult state = MatchResult.MATCH;
		for(Matchable m : matches){
			MatchResult r = evaluate(context, m);
			if(r == MatchResult.INDETERMINATE &&
					state == MatchResult.MATCH){
				if(log.isDebugEnabled()){
					log.debug("Match result=\"{}\" " +
							"continue to match", r);
				}
				state = r;
				continue;
			}
			if(r == MatchResult.NOMATCH){
				state = r;
				if(log.isDebugEnabled()){
					log.debug("Match result=\"{}\" " +
							"stop match", r);
				}
				break;
			}
		}
		return state;
	}

	private MatchResult evaluate(EvaluationContext context, Matchable m){
		try{
			return m.match(context);
		}catch (Exception e){
			return MatchResult.INDETERMINATE;
		}
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("Matches", matches)
				.toString();
	}

	@Override
	public int hashCode(){
		return matches.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof MatchAllOf)){
			return false;
		}
		MatchAllOf m = (MatchAllOf)o;
		return matches.equals(m.matches);
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		for(Matchable m : matches){
			m.accept(v);
		}
		v.visitLeave(this);
	}

	public static class Builder
	{
		private ImmutableList.Builder<Match> matches = ImmutableList.builder();

		private Builder(){
		}

		public Builder allOf(Match ...match){
			this.matches.add(match);
			return this;
		}

		public Builder allOf(Iterable<Match> match){
			this.matches.addAll(match);
			return this;
		}

		public Builder match(
				Value value,
				AttributeReference ref, FunctionSpec predicate)
		{
			return allOf(Match.builder().predicate(predicate).attribute(value).attrRef(ref).build());
		}

		public MatchAllOf build(){
			return new MatchAllOf(this);
		}
	}
}
