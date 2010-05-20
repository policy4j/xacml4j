package com.artagon.xacml.v3.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;
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
	public DefaultAttribute(
			String attributeId,
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
	
	public DefaultAttribute(String attributeId,
			String issuer, 
			boolean includeInResult, 
			AttributeValue ...values){
		this(attributeId, issuer, 
				includeInResult, Arrays.asList(values));
	}
	
	public DefaultAttribute(String attributeId, 
			Collection<AttributeValue> values){
		this(attributeId, null, false, values);
	}
	
	public DefaultAttribute(String attributeId, 
			AttributeValue ...values){
		this(attributeId, null, false, Arrays.asList(values));
	}
	
	@Override
	public String getAttributeId(){
		return attributeId;
	}
	
	@Override
	public Collection<AttributeValue> getValues(){
		return Collections.unmodifiableCollection(values);
	}
	
	@Override
	public String getIssuer(){
		return issuer;
	}
	
	@Override
	public boolean isIncludeInResult(){
		return includeInResult;
	}
}
