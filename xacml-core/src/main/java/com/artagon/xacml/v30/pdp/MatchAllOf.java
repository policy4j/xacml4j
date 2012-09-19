package com.artagon.xacml.v30.pdp;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.MatchResult;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

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
	public MatchAllOf(Collection<Match> match){
		Preconditions.checkNotNull(match);
		Preconditions.checkArgument(match.size() >= 1);
		this.matches = new LinkedList<Match>(match);
	}

	public MatchAllOf(Match ... matches){
		this(Arrays.asList(matches));
	}

	public static Builder builder(){
		return new Builder();
	}

	public Collection<Match> getMatch(){
		return Collections.unmodifiableCollection(matches);
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
		private Collection<Match> matches = new LinkedList<Match>();

		private Builder(){
		}

		public Builder match(Match ...match){
			Preconditions.checkNotNull(match);
			for(Match m : match){
				this.matches.add(m);
			}
			return this;
		}

		public Builder match(Iterable<Match> match){
			Preconditions.checkNotNull(match);
			for(Match m : match){
				this.matches.add(m);
			}
			return this;
		}

		public Builder withMatch(AttributeExp value,
				AttributeReference ref, FunctionSpec predicate)
		{
			return match(new Match(predicate, value, ref));
		}

		public MatchAllOf create(){
			return new MatchAllOf(matches);
		}
	}
}
