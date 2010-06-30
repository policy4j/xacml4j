package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

abstract class BaseDecisionRuleResponse extends XacmlObject
{
	private String id;
	private Multimap<String, AttributeAssignment> attributes;
	
	protected BaseDecisionRuleResponse(
			String id, Collection<AttributeAssignment> attributes) 
	{
		checkNotNull(id, "Decision rule response id can not be null");
		checkNotNull(attributes, 
				"Decision rule attribute assignments can not be null");
		this.id = id;
		this.attributes = LinkedHashMultimap.create();
		for(AttributeAssignment a : attributes){
			Preconditions.checkArgument(a != null, 
					"Decision rule with id=\"%s\" " +
					"attribute assignment can not be null", id);
			this.attributes.put(a.getAttributeId(), a);
		}
	}
	
	public final String getId(){
		return id;
	}
		
	public final Collection<AttributeAssignment> getAttributes(){
		return Collections.unmodifiableCollection(attributes.values());
	}
	
	public final Collection<AttributeAssignment> getAttribute(String attributeId){
		return this.attributes.get(attributeId);
	}
}
