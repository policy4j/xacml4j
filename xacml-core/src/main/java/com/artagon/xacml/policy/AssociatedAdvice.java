package com.artagon.xacml.policy;

import java.util.Collection;
import java.util.LinkedList;

public class AssociatedAdvice implements PolicyElement
{
	private Collection<Advice> advice;
	
	public AssociatedAdvice(Collection<Advice> advice){
		this.advice = new LinkedList<Advice>(advice);
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(Advice a : advice){
			a.accept(v);
		}
		v.visitLeave(this);
	}
}
