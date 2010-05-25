package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.MatchAllOf;
import com.artagon.xacml.v3.MatchAnyOf;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.Matchable;
import com.artagon.xacml.v3.PolicyElement;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

final class DefaultMatchAnyOf extends XacmlObject 
	implements PolicyElement, MatchAnyOf
{
	private Collection<MatchAllOf> matches;
	
	public DefaultMatchAnyOf(Collection<MatchAllOf> matches){
		Preconditions.checkNotNull(matches);
		Preconditions.checkArgument(matches.size() >= 1);
		this.matches = new LinkedList<MatchAllOf>(matches);
	}
	
	@Override
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
