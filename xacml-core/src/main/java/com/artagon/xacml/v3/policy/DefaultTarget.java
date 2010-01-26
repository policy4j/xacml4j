package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class DefaultTarget implements Target
{
	private Collection<Matchable> matches;
	
	DefaultTarget(Collection<Matchable> matches){
		this.matches = new LinkedList<Matchable>(matches);
	}
	
	public DefaultTarget(){
		this(Collections.<Matchable>emptyList());
	}
	
	public MatchResult match(EvaluationContext context) 
	{
		MatchResult state = MatchResult.MATCH;
		for(Matchable m : matches){
			MatchResult r = m.match(context);
			if(r == MatchResult.NOMATCH){
				state = r;
				break;
			}
			if(r == MatchResult.INDETERMINATE){
				state = r;
				continue;
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
