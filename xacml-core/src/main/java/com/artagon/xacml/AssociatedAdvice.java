package com.artagon.xacml;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class AssociatedAdvice
{
	private Collection<Advice> advice;
	
	public AssociatedAdvice(Collection<Advice> advice){
		this.advice = new LinkedList<Advice>(advice);
	}
	
	public Collection<Advice> getAdvice(){
		return Collections.unmodifiableCollection(advice);
	}
}
