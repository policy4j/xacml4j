package com.artagon.xacml.v3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import org.w3c.dom.Node;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class Attributes extends XacmlObject
{
	private String id;
	private AttributeCategoryId categoryId;
	private Node content;
	private Multimap<String, Attribute> attributes;
	
	/**
	 * Constructs an attributes with a given identifier,
	 * category, attributes and XML content
	 * 
	 * @param id an optional unique identifier
	 * @param categoryId an attribute category
	 * @param content an optional XML content
	 * @param attributes a collection of attributes
	 */
	public Attributes(String id, AttributeCategoryId categoryId, 
			Node content, Collection<Attribute> attributes){
		Preconditions.checkNotNull(categoryId);
		Preconditions.checkNotNull(attributes);
		this.id = id;
		this.categoryId = categoryId;
		this.attributes = HashMultimap.create();
		for(Attribute attr : attributes){
			this.attributes.put(attr.getAttributeId(), attr);
		}
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public Attributes(String id, AttributeCategoryId categoryId, 
			Node content, Attribute ...attributes){
		this(id, categoryId, content, Arrays.asList(attributes));
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public Attributes(AttributeCategoryId categoryId, 
			Collection<Attribute> attributes){
		this(null, categoryId, null, attributes);
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public Attributes(String id, AttributeCategoryId categoryId, 
			Collection<Attribute> attributes){
		this(id, categoryId, null, attributes);
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public Attributes(String id, AttributeCategoryId categoryId, 
			Attribute ...attributes){
		this(id, categoryId, null, Arrays.asList(attributes));
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public Attributes(AttributeCategoryId categoryId, 
			Attribute ...attributes){
		this(null, categoryId, null, Arrays.asList(attributes));
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
	 * Gets an attribute category
	 * 
	 * @return attribute category
	 */
	public AttributeCategoryId getCategoryId(){
		return categoryId;
	}
	
	public Set<String> getProvidedAttributeIds(){
		return Collections.unmodifiableSet(attributes.keySet());
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
	 * Gets a collection of attributes by identifier
	 * 
	 * @param attributeId an attribute id
	 * @return a collection of {@link Attribute}
	 * instances or an empty collection if no
	 * matching attributes found
	 */
	public Collection<Attribute> getAttribute(String attributeId){
		return attributes.get(attributeId);
	}
	
	public Collection<Attribute> getAttributes(final String attributeId, final String issuer){
		Collection<Attribute> v = attributes.get(attributeId);
		return  Collections2.filter(v, new Predicate<Attribute>() {
			@Override
			public boolean apply(Attribute attr) {
				return Objects.equal(issuer, attr.getIssuer());
			}
		});	
	}
	
	public BagOfAttributeValues<? extends AttributeValue> getAttributeValues(
			String attributeId, String issuer, AttributeValueType type){
		Preconditions.checkNotNull(type);
		Collection<Attribute> attrs = getAttributes(attributeId, issuer);
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(Attribute a : attrs){
			values.addAll(a.getValuesByType(type));
		}
		return type.bagOf().create(values);
	}
		
	public Node getContent(){
		return content;
	}
}
