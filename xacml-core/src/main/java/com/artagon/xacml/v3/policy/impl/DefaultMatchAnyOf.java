package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.MatchAllOf;
import com.artagon.xacml.v3.policy.MatchAnyOf;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.Matchable;
import com.artagon.xacml.v3.policy.PolicyElement;
import com.artagon.xacml.v3.policy.PolicyVisitor;

final class DefaultMatchAnyOf extends XacmlObject 
	implements PolicyElement, MatchAnyOf
{
	private Collection<Matchable> matches;
	
	public DefaultMatchAnyOf(Collection<MatchAllOf> matches){
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
