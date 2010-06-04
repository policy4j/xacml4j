package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

public class MatchAnyOf extends XacmlObject 
	implements PolicyElement, Matchable
{
	private Collection<MatchAllOf> matches;
	
	public MatchAnyOf(Collection<MatchAllOf> matches)
	{
		Preconditions.checkNotNull(matches);
		Preconditions.checkArgument(matches.size() >= 1);
		this.matches = new LinkedList<MatchAllOf>(matches);
	}
	
	public Collection<MatchAllOf> getAllOf(){
		return Collections.unmodifiableCollection(matches);
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
