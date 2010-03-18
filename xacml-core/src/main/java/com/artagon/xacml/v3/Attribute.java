package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class Attribute extends XacmlObject
{
	private String id;
	private String attributeId;
	private Multiset<AttributeValue> values;
	private boolean includeInResult;
	private String issuer;
	
	public Attribute(String id, String attributeId, 
			Collection<AttributeValue> values, 
			String issuer, boolean includeInResult){
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(values);
		this.id = id;
		this.attributeId = attributeId;
		this.values = HashMultiset.create(values.size());
		this.values.addAll(values);
		this.includeInResult = includeInResult;
	}
	
	public Attribute(String id, String attributeId, 
			Collection<AttributeValue> values){
		this(id, attributeId, values, null, false);
	}
	
	/**
	 * An unique identifier of the attribute in
	 * the request context
	 * 
	 * @return unique identifier of the
	 * attribute in the request context
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * Gets attribute identifier.
	 * 
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/**
	 * Gets attribute values as collection of
	 * {@link AttributeValue} instances
	 * 
	 * @return collection of {@link AttributeValue} 
	 * instances
	 */
	public Collection<AttributeValue> getValues(){
		return Collections.unmodifiableCollection(values);
	}
	
	public String getIssuer(){
		return issuer;
	}
	
	public boolean isIncludeInResult(){
		return includeInResult;
	}
	
	public Collection<AttributeValue> getValuesByType(final AttributeValueType type){
		return Collections2.filter(values, new Predicate<AttributeValue>() {
			@Override
			public boolean apply(AttributeValue v) {
				return v.getType().equals(type);
			}
		});
	}
}
