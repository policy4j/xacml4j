package com.artagon.xacml.v3.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class DefaultAttribute extends XacmlObject implements Attribute
{
	private String attributeId;
	private Multiset<AttributeValue> values;
	private boolean includeInResult;
	private String issuer;
	
	/**
	 * Constructs attribute with given
	 * parameters
	 * 
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @param includeInResult a flag indicating
	 * if attribute needs to be included in
	 * the result
	 * @param values a collection of 
	 * {@link AttributeValue} instances
	 */
	public DefaultAttribute(String attributeId,
			String issuer, 
			boolean includeInResult, 
			Collection<AttributeValue> values){
		Preconditions.checkNotNull(attributeId);
		Preconditions.checkNotNull(values);
		this.attributeId = attributeId;
		this.issuer = issuer;
		this.values = HashMultiset.create(values.size());
		this.values.addAll(values);
		this.includeInResult = includeInResult;
	}
	
	/**
	 * Constructs attribute with a given
	 * identifier and values
	 * 
	 * @param attributeId an identifier
	 * for this attribute
	 * @param values a collection of
	 * {@link AttributeValue} instances
	 */
	public DefaultAttribute(String attributeId, 
			Collection<AttributeValue> values){
		this(attributeId, null, false, values);
	}
	
	/**
	 * 
	 * @param attributeId an identifier
	 * @param values a collection of
	 */
	public DefaultAttribute(String attributeId, 
			AttributeValue ...values){
		this(attributeId, null, false, Arrays.asList(values));
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.Attribute#getAttributeId()
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.Attribute#getValues()
	 */
	public Collection<AttributeValue> getValues(){
		return Collections.unmodifiableCollection(values);
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.Attribute#getIssuer()
	 */
	public String getIssuer(){
		return issuer;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.Attribute#isIncludeInResult()
	 */
	public boolean isIncludeInResult(){
		return includeInResult;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.Attribute#getValuesByType(com.artagon.xacml.v3.policy.AttributeValueType)
	 */
	public Collection<AttributeValue> getValuesByType(final AttributeValueType type){
		return Collections2.filter(values, new Predicate<AttributeValue>() {
			@Override
			public boolean apply(AttributeValue v) {
				return v.getType().equals(type);
			}
		});
	}
}
