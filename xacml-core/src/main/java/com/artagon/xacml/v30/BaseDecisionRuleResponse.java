package com.artagon.xacml.v30;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;


public abstract class BaseDecisionRuleResponse
{
	protected String id;
	protected Multimap<String, AttributeAssignment> attributes;
	
	/**
	 * For compatibility with XACML 2.0
	 * response context
	 */
	private Effect effect;
	
	private transient int hashCode;
	
	protected BaseDecisionRuleResponse(
			String id, 
			Effect effect,
			Iterable<AttributeAssignment> attributes) 
	{
		checkNotNull(id, "Decision rule response id can not be null");
		checkNotNull(attributes, 
				"Decision rule attribute assignments can not be null");
		this.id = id;
		this.attributes = LinkedHashMultimap.create();
		this.effect = effect;
		for(AttributeAssignment a : attributes){
			checkArgument(a != null, 
					"Decision rule with id=\"%s\" " +
					"attribute assignment can not be null", id);
			this.attributes.put(a.getAttributeId(), a);
		}
		this.attributes = Multimaps.unmodifiableMultimap(this.attributes);
		this.hashCode = Objects.hashCode(id, attributes);
	}
	
	public final String getId(){
		return id;
	}
	
	/**
	 * For compatibility with XACML 2.0 response
	 * context
	 * 
	 * @return {@link Effect}
	 */
	public Effect getFullfillOn(){
		return effect;
	}
	
	public final Collection<AttributeAssignment> getAttributes(){
		return attributes.values();
	}
	
	public final Collection<AttributeAssignment> getAttribute(String attributeId){
		return this.attributes.get(attributeId);
	}
	
	@Override
	public final int hashCode(){
		return hashCode;
	}
	
	@Override
	public final String toString(){
		return Objects.toStringHelper(this)
		.add("id", id)
		.add("attributes", attributes)
		.toString();
	}
}
