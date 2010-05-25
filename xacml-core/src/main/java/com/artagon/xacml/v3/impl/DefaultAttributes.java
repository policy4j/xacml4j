package com.artagon.xacml.v3.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class DefaultAttributes extends XacmlObject implements Attributes
{
	private String id;
	private AttributeCategoryId categoryId;
	private Node content;
	private Multimap<String, Attribute> attributes;
	private String scope;
	
	/**
	 * Constructs an attributes with a given identifier,
	 * category, attributes and XML content
	 * 
	 * @param id an optional unique identifier
	 * @param categoryId an attribute category
	 * @param content an optional XML content
	 * @param attributes a collection of attributes
	 */
	public DefaultAttributes(
			String id, 
			AttributeCategoryId categoryId, 
			Node content, Iterable<Attribute> attributes){
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
	public DefaultAttributes(String id, AttributeCategoryId categoryId, 
			Node content, Attribute ...attributes){
		this(id, categoryId, content, Arrays.asList(attributes));
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public DefaultAttributes(AttributeCategoryId categoryId, 
			Iterable<Attribute> attributes){
		this(null, categoryId, null, attributes);
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public DefaultAttributes(String id, AttributeCategoryId categoryId, 
			Iterable<Attribute> attributes){
		this(id, categoryId, null, attributes);
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public DefaultAttributes(String id, AttributeCategoryId categoryId, 
			Attribute ...attributes){
		this(id, categoryId, null, Arrays.asList(attributes));
	}
	
	/** 
	 * @see {@link #Attributes(String, AttributeCategoryId, Node, Collection)
	 */
	public DefaultAttributes(AttributeCategoryId categoryId, 
			Attribute ...attributes){
		this(null, categoryId, null, Arrays.asList(attributes));
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public String getScope(){
		return scope;
	}
	
	@Override
	public Node getContent(){
		return content;
	}
	
	@Override
	public AttributeCategoryId getCategoryId(){
		return categoryId;
	}
	
	@Override
	public boolean containsAttribute(String attributeId){
		return attributes.containsKey(attributeId);
	}
	
	@Override
	public Collection<Attribute> getAttributes(String attributeId){
		return attributes.get(attributeId);
	}
	
	@Override
	public Collection<Attribute> getAttributes(final String attributeId, final String issuer){
		Collection<Attribute> v = attributes.get(attributeId);
		return  Collections2.filter(v, new Predicate<Attribute>() {
			@Override
			public boolean apply(Attribute attr) {
				return issuer == null || 
				issuer.equals(attr.getIssuer());
			}
		});	
	}
	
	@Override
	public Collection<Attribute> getAttributes() {
		return Collections.unmodifiableCollection(attributes.values());
	}

	@Override
	public Collection<Attribute> getIncludeInResultAttributes(){
		return  Collections2.filter(attributes.values(), new Predicate<Attribute>() {
			@Override
			public boolean apply(Attribute attr) {
				return attr.isIncludeInResult();
			}
		});	
	}
	
	@Override
	public Collection<AttributeValue> getAttributeValues(
			String attributeId, String issuer, final AttributeValueType type){
		Preconditions.checkNotNull(type);
		Collection<Attribute> attrs = getAttributes(attributeId, issuer);
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(Attribute a : attrs)
		{
			values.addAll(Collections2.filter(a.getValues(), new Predicate<AttributeValue>() {
				@Override
				public boolean apply(AttributeValue attr) {
					return type.equals(attr.getType());
				}
			}));
		}
		return Collections.unmodifiableCollection(values);
	}
}
