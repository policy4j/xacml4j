package com.artagon.xacml.v3.spi.pip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;

public final class AttributeResolverDescriptorBuilder 
{
	private Map<String, AttributeDescriptor> attributes;
	private String issuer;
	private Set<AttributeCategoryId> categories;
	
	private AttributeResolverDescriptorBuilder(AttributeCategoryId category, String issuer){
		Preconditions.checkArgument(category != null);
		this.issuer = issuer;
		this.categories = new HashSet<AttributeCategoryId>();
		this.categories.add(category);
		this.attributes = new HashMap<String, AttributeDescriptor>();
	}
	
	public static AttributeResolverDescriptorBuilder create(AttributeCategoryId category, String issuer){
		return new AttributeResolverDescriptorBuilder(category, issuer);
	}
	
	public static AttributeResolverDescriptorBuilder create(AttributeCategoryId category){
		return create(category, null);
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId, AttributeValueType dataType){
		AttributeDescriptor d = new AttributeDescriptor(attributeId, dataType);
		attributes.put(d.getAttributeId(), d);
		return this;
	}
	
	public AttributeResolverDescriptorBuilder category(AttributeCategoryId category)
	{
		categories.add(category);
		return this;
	}
	
	public AttributeResolverDescriptorBuilder attribute(
			String attributeId, XacmlDataTypes dataType){
		AttributeDescriptor d = new AttributeDescriptor(attributeId, dataType.getType());
		attributes.put(d.getAttributeId(), d);
		return this;
	}
		
	public AttributeResolverDescriptor build(){
		return new AttributeResolverDescriptorImpl(issuer, categories, attributes);
	}
}
