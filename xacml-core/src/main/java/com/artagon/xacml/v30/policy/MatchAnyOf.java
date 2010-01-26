package com.artagon.xacml.v30.policy;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;

public final class MatchAnyOf implements Matchable, PolicyElement
{
	private Collection<? extends Matchable> matches;
	
	public MatchAnyOf(Collection<? extends Matchable> matches){
		Preconditions.checkNotNull(matches);
		Preconditions.checkArgument(matches.size() >= 1);
		this.matches = new LinkedList<Matchable>(matches);
	}
	
	@Override
	public MatchResult match(EvaluationContext context) 
	{
		MatchResult state = MatchResult.NOMATCH;
		for(Matchable m : matches){
			MatchResult result = m.match(context);
			if(result == MatchResult.INDETERMINATE){
				state = MatchResult.INDETERMINATE;
				continue;
			}
			if(result == MatchResult.MATCH){
				state = MatchResult.MATCH;
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
