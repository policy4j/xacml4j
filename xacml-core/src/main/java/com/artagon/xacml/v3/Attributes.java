package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Node;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;


public class Attributes extends XacmlObject
{
	private String id;
	private AttributeCategoryId categoryId;
	private Node content;
	private Multimap<String, Attribute> attributes;
	
	
	public String getId(){
		return id;
	}

	public AttributeCategoryId getCategoryId(){
		return categoryId;
	}
	
	public boolean containsAttribute(String attributeId){
		return attributes.containsKey(attributeId);
	}
	
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
