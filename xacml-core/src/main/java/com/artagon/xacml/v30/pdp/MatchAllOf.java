package com.artagon.xacml.v30.pdp;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.MatchResult;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class MatchAllOf implements PolicyElement, Matchable
{
	private final static Logger log = LoggerFactory.getLogger(MatchAllOf.class);

	private Collection<Match> matches;

	/**
	 * Constructs a {@link MatchAllOf} with
	 * a given collection of {@link Match}
	 * elements
	 *
	 * @param match a collection of {@link Match}
	 * instances
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
			MatchResult r = m.match(context);
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

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("Matches", matches)
				.toString();
	}

	@Override
	public int hashCode(){
		return matches.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof MatchAllOf)){
			return false;
		}
		MatchAllOf m = (MatchAllOf)o;
		return matches.equals(m.matches);
	}

	@Override
	public void accept(PolicyVisitor v) {
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

		public Builder match(AttributeExp value,
				AttributeReference ref, FunctionSpec predicate)
		{
			return allOf(new Match(predicate, value, ref));
		}

		public MatchAllOf build(){
			return new MatchAllOf(this);
		}
	}
}
