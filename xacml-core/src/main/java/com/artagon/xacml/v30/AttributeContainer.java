package com.artagon.xacml.v30;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class AttributeContainer 
{
	protected Multimap<String, Attribute> attributes;
	
	protected AttributeContainer(Iterable<Attribute> attributes){
		this.attributes = LinkedListMultimap.create();
		for(Attribute attr : attributes){
			this.attributes.put(attr.getAttributeId(), attr);
		}
		this.attributes = Multimaps.unmodifiableMultimap(this.attributes);
	}
	
	/**
	 * Tests if this instance contains an
	 * attribute with a given identifier
	 * 
	 * @param attributeId an attribute id
	 * @return <code>true</code> if contains
	 */
	public boolean containsAttribute(String attributeId){
		return attributes.containsKey(attributeId);
	}
	
	/**
	 * Gets all attributes with a given identifier
	 * 
	 * @param attributeId an attribute identifier
	 * @return a collection of attributes if there is no
	 * attributes with a given identifier empty collection
	 * is returned
	 */
	public Collection<Attribute> getAttributes(String attributeId){
		return  Collections.unmodifiableCollection(attributes.get(attributeId));
	}
	
	/**
	 * Gets a single {@link Attribute} instance with
	 * a given attribute identifier
	 * 
	 * @param attributeId an attribute identifier
	 * @return {@link Attribute} instance or <code>null</code>
	 * if no attribute available with a given identifier
	 */
	public Attribute getOnlyAttribute(String attributeId){
		return Iterables.getOnlyElement(attributes.get(attributeId), null);
	}
	
	/**
	 * Gets all attributes with a given attribute 
	 * identifier and issuer
	 * 
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @return a collection of attributes with a given identifier
	 * and given issuer
	 */
	public Collection<Attribute> getAttributes(final String attributeId, final String issuer){
		Collection<Attribute> v = attributes.get(attributeId);
		return Collections.unmodifiableCollection(
				Collections2.filter(v, new Predicate<Attribute>() {
					@Override
					public boolean apply(Attribute attr) {
						return issuer == null || 
						issuer.equals(attr.getIssuer());
					}}));	
	}
	
	/**
	 * Gets all attribute {@Link Attribute}
	 * instances
	 * 
	 * @return immutable collection of {@link Attribute}
	 * instances
	 */
	public Collection<Attribute> getAttributes() {
		return Collections.unmodifiableCollection(attributes.values());
	}
	
	/**
	 * Finds all instance of {@Link Attribute} with
	 * {@link #isIncludeInResult()} returning
	 * <code>true</code>
	 * 
	 * @return a collection of {@link Attribute}
	 * instances
	 */
	public Collection<Attribute> getIncludeInResultAttributes(){
		return Collections.unmodifiableCollection(
				Collections2.filter(attributes.values(), new Predicate<Attribute>() {
				@Override
				public boolean apply(Attribute attr) {
					return attr.isIncludeInResult();
				}
			}));	
	}
	
	/**
	 * @see {@link #getAttributeValues(String, String, AttributeValueType)
	 */
	public Collection<AttributeValue> getAttributeValues(
			String attributeId, 
			AttributeValueType dataType){
		return getAttributeValues(attributeId, null, dataType);
	}
	
	/**
	 * Gets only one {@link AttributeValue} instance of the given type 
	 * from an attribute with a given identifier
	 * 
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute value data type
	 * @return {@link AttributeValue} of the given type or <code>null</code>
	 * if value matching given criteria does not exist
	 * @exception IllegalArgumentException if more than one value is found
	 * matching given criteria
	 */
	public AttributeValue getOnlyAttributeValue(String attributeId, 
			AttributeValueType dataType){
		return Iterables.getOnlyElement(getAttributeValues(attributeId, dataType), null);
	}
	
	/**
	 * Gets all {@link AttributeValue} instances
	 * contained in this attributes instance
	 * 
	 * @param attributeId an attribute id
	 * @param issuer an attribute issuer
	 * @param type an attribute value data type
	 * @return a collection of {@link AttributeValue} instances
	 */
	public Collection<AttributeValue> getAttributeValues(
			String attributeId, String issuer, final AttributeValueType type){
		Preconditions.checkNotNull(type);
		Collection<Attribute> found = getAttributes(attributeId, issuer);
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(Attribute a : found){
			values.addAll(a.getValuesByType(type));
		}
		return values;
	}
}
