package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.Match;
import com.artagon.xacml.v3.policy.MatchAllOf;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.Matchable;
import com.artagon.xacml.v3.policy.PolicyElement;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.google.common.base.Preconditions;

final class DefaultMatchAllOf extends XacmlObject 
	implements PolicyElement, MatchAllOf
{
	private final static Logger log = LoggerFactory.getLogger(DefaultMatchAllOf.class);
	
	private Collection<Matchable> matches;
	
	public DefaultMatchAllOf(Collection<Match> match){
		Preconditions.checkNotNull(match);
		Preconditions.checkArgument(match.size() >= 1);
		this.matches = new LinkedList<Matchable>(match);
	}

	@Override
	public MatchResult match(EvaluationContext context)
	{
		MatchResult state = MatchResult.MATCH;
		Preconditions.checkState(matches.size() >= 1);
		log.debug("Trying to match=\"{}\" matchables", matches.size());
		for(Matchable m : matches)
		{
			MatchResult r = m.match(context);
			if(r == MatchResult.INDETERMINATE && 
					state == MatchResult.MATCH){
				log.debug("Match result=\"{}\" continue to match", r);
				state = r;
				continue;
			}
			if(r == MatchResult.NOMATCH){
				state = r;
				log.debug("Match result=\"{}\" stop match", r);
				break;
			}
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
}
