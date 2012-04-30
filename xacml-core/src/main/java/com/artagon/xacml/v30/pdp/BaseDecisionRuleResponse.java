package com.artagon.xacml.v30.pdp;

import java.util.Collection;

import com.artagon.xacml.v30.AttributeCategory;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;


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
			BaseBuilder<?> b) 
	{
		this.id = b.id;
		this.attributes = ImmutableSetMultimap.copyOf(b.attributes);
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
	@Deprecated
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
	
	public abstract static class BaseBuilder <T extends BaseBuilder<?>>
	{
		protected String id;
		protected Effect appliesTo;
		protected Multimap<String, AttributeAssignment> attributes = LinkedHashMultimap.create();
		
		protected BaseBuilder(String id, Effect effect){
			Preconditions.checkNotNull(id);
			this.id = id;
			this.appliesTo = effect;
		}
		
		public final T attribute(AttributeAssignment attr){
			Preconditions.checkNotNull(attr);
			this.attributes.put(attr.getAttributeId(), attr);
			return getThis();
		}
		
		public final T attributes(Iterable<AttributeAssignment> attributes){
			for(AttributeAssignment attr : attributes){
				this.attributes.put(attr.getAttributeId(), attr);
			}
			return getThis();
		}
		
		public final T attribute(
				String id, AttributeExp ... values)
		{
			return attribute(id, null, null, values);
		}
		
		public final T attribute(
				String id,  
				AttributeCategory category, 
				String issuer, 
				AttributeExp ... values)
		{
			if(values == null || 
					values.length == 0){
				return getThis();
			}
			for(AttributeExp v : values){
				attribute(new AttributeAssignment(id, category, issuer, v));
			}
			return getThis();
		}
		
		protected abstract T getThis();
	}
}
