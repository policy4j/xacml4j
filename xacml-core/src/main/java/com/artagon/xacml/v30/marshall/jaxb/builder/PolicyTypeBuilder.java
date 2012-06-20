package com.artagon.xacml.v30.marshall.jaxb.builder;

import java.util.Collection;
import java.util.LinkedList;

import org.oasis.xacml.v30.jaxb.RuleType;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class PolicyTypeBuilder 
{
	
	
	private String id;
	private Collection<RuleType> rules = new LinkedList<RuleType>();

	private PolicyTypeBuilder(){
	}
	
	public PolicyTypeBuilder withId(String id){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
		this.id = id;
		return this;
	}
	
	public PolicyTypeBuilder withRule(RuleTypeBuilder b)
	{
		this.rules.add(b.build());
		return this;
	}
}
