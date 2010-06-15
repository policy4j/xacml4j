package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class BaseDecisionRuleResponse extends XacmlObject
{
	private String id;
	private Map<String, AttributeAssignment> attributes;
	
	protected BaseDecisionRuleResponse(
			String id, Collection<AttributeAssignment> attributes) 
	{
		checkNotNull(id, "Decision rule response id can not be null");
		checkNotNull(attributes, 
				"Decision rule attribute assignments can not be null");
		this.id = id;
		this.attributes = new LinkedHashMap<String, AttributeAssignment>();
		for(AttributeAssignment a : attributes)
		{
			AttributeAssignment old = this.attributes.put(a.getAttributeId(), a);
			if(old != null){
				throw new IllegalArgumentException(
						String.format(
						"Decision rule response already " +
						"contains attribute assignment with id=\"%s\"", 
					old.getAttributeId()));
			}
		}
	}
	
	public final String getId(){
		return id;
	}
	
	public final Collection<AttributeAssignment> getAttributes(){
		return Collections.unmodifiableCollection(attributes.values());
	}
	
	public final AttributeAssignment getAttribute(String id){
		return this.attributes.get(id);
	}
}
