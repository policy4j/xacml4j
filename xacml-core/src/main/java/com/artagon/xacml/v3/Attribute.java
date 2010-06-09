package com.artagon.xacml.v3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * A XACML request context attribute
 * 
 * @author Giedrius Trumpickas
 */
public class Attribute extends XacmlObject
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
	public Attribute(
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
	
	public Attribute(String attributeId,
			String issuer, 
			boolean includeInResult, 
			AttributeValue ...values){
		this(attributeId, issuer, 
				includeInResult, Arrays.asList(values));
	}
	
	public Attribute(String attributeId, 
			Collection<AttributeValue> values){
		this(attributeId, null, false, values);
	}
	
	public Attribute(String attributeId, 
			AttributeValue ...values){
		this(attributeId, null, false, Arrays.asList(values));
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
	
	/**
	 * Gets this attribute issuer
	 * 
	 * @return issuer of this attribute
	 * identifier or <code>null</code>
	 * if it's not available
	 */
	public String getIssuer(){
		return issuer;
	}
	
	/**
	 * Tests if this attribute needs
	 * to be included back to the
	 * evaluation result
	 * 
	 * @return <code>true</code>
	 * if needs to be included
	 */
	public boolean isIncludeInResult(){
		return includeInResult;
	}
}
