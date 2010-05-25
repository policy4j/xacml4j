package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Match;
import com.artagon.xacml.v3.MatchAllOf;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.Matchable;
import com.artagon.xacml.v3.PolicyElement;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

final class DefaultMatchAllOf extends XacmlObject 
	implements PolicyElement, MatchAllOf
{
	private final static Logger log = LoggerFactory.getLogger(DefaultMatchAllOf.class);
	
	private Collection<Match> matches;
	
	public DefaultMatchAllOf(Collection<Match> match){
		Preconditions.checkNotNull(match);
		Preconditions.checkArgument(match.size() >= 1);
		this.matches = new LinkedList<Match>(match);
	}

	@Override
	public Collection<Match> getMatch(){
		return Collections.unmodifiableCollection(matches);
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
