package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

public class MatchAllOf extends XacmlObject 
	implements PolicyElement, Matchable
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
	public MatchAllOf(Collection<Match> match)
	{
		Preconditions.checkNotNull(match);
		Preconditions.checkArgument(match.size() >= 1);
		this.matches = new LinkedList<Match>(match);
	}

	public Collection<Match> getMatch(){
		return Collections.unmodifiableCollection(matches);
	}
	
	@Override
	public MatchResult match(EvaluationContext context)
	{
		MatchResult state = MatchResult.MATCH;
		Preconditions.checkState(matches.size() >= 1);
		if(log.isDebugEnabled()){
			log.debug("Trying to match=\"{}\"" +
					" matchables", matches.size());
		}
		for(Matchable m : matches)
		{
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
	public void accept(PolicyVisitor v) {		
		v.visitEnter(this);
		for(Matchable m : matches){
			m.accept(v);
		}
		v.visitLeave(this);
	}
}
