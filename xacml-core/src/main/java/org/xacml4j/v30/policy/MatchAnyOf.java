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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyElement;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class MatchAnyOf
		implements PolicyElement, Matchable
{
	private final static Logger log = LoggerFactory.getLogger(MatchAnyOf.class);

	private final Collection<MatchAllOf> allOfs;

	private MatchAnyOf(Builder b){
		this.allOfs = b.allMatchAllOfs.build();
		Preconditions.checkArgument(allOfs.size() >= 1, "At least one MatchAllOf defaultProvider is required");
	}

	public Collection<MatchAllOf> getAllOf(){
		return allOfs;
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public MatchResult match(EvaluationContext context)
	{
		MatchResult state = MatchResult.NOMATCH;
		for(Matchable m : allOfs){
			MatchResult result = m.match(context);
			if(result == MatchResult.INDETERMINATE){
				if(log.isDebugEnabled()){
					log.debug("AnyOf matchable match result=\"{}\"", result);
				}
				state = MatchResult.INDETERMINATE;
				continue;
			}
			if(result == MatchResult.MATCH){
				if(log.isDebugEnabled()){
					log.debug("AnyOf matchable match result=\"{}\"", result);
				}
				state = MatchResult.MATCH;
				break;
			}
		}
		return state;
	}

	public void accept(PolicyTreeVisitor v) {
		v.visitEnter(this);
		for(Matchable m : allOfs){
			m.accept(v);
		}
		v.visitLeave(this);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("AllOf", allOfs)
				.toString();
	}

	@Override
	public int hashCode(){
		return allOfs.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof MatchAnyOf)){
			return false;
		}
		MatchAnyOf m = (MatchAnyOf)o;
		return allOfs.equals(m.allOfs);
	}



	public static class Builder{
		private ImmutableList.Builder<MatchAllOf> allMatchAllOfs = ImmutableList.builder();

		private Builder(){
		}

		public Builder anyOf(MatchAllOf ...allOf){
			Preconditions.checkNotNull(allOf);
			this.allMatchAllOfs.add(allOf);
			return this;
		}

		public Builder anyOf(Iterable<MatchAllOf> allOfs){
			this.allMatchAllOfs.addAll(allOfs);
			return this;
		}

		public MatchAnyOf build(){
			return new MatchAnyOf(this);
		}
	}
}
