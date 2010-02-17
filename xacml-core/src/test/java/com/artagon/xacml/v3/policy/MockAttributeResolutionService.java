package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;



public class MockAttributeResolutionService implements AttributeResolver
{
	private Map<AttributeCategoryId, Map<String, AttributeHolder>> attributes;
	
	public MockAttributeResolutionService(){
		this.attributes = new HashMap<AttributeCategoryId, Map<String,AttributeHolder>>();
	}
	
	@Override
	public BagOfAttributeValues<?> resolve(
			AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType, String issuer) 
	{
		Map<String, AttributeHolder> byCategory = attributes.get(category);
		if(byCategory == null){
			return dataType.bagOf().createEmpty();
		}
		AttributeHolder holder =  byCategory.get(attributeId);
		if(holder == null){
			return dataType.bagOf().createEmpty();
		}
		if(issuer != null && issuer.equals(holder.issuer)){
			if(dataType.equals(holder.bag.getAttributeType())){
				return holder.bag;
			}
		}
		return dataType.bagOf().createEmpty();
	}
	
	@Override
	public BagOfAttributeValues<?> resolve(AttributeCategoryId category, XPath location,
			AttributeValueType dataType) {
		return dataType.bagOf().createEmpty();
	}
	
	public void addAttribute(
			AttributeCategoryId category, 
			String attributeId, 
			String issuer,
			AttributeValueType type, 
			Collection<AttributeValue> attributeValues){
		AttributeHolder holder = new AttributeHolder(issuer, category, type, attributeValues);
		Map<String, AttributeHolder> byCategory = attributes.get(category);
		if(byCategory == null){
			byCategory = new HashMap<String, AttributeHolder>();
			attributes.put(category, byCategory);
		}
		byCategory.put(attributeId, holder);
		
	}
	
	
	
	@Override
	public Node getContent(AttributeCategoryId categoryId) {
		// TODO Auto-generated method stub
		return null;
	}



	class AttributeHolder
	{
		String issuer;
		BagOfAttributeValues<?> bag;
		AttributeCategoryId category;
		
		public AttributeHolder(
				String issuer, 
				AttributeCategoryId category,
				AttributeValueType type, Collection<AttributeValue> attributes) 
		{
			this.issuer = issuer;
			this.category = category;
			this.bag = type.bagOf().createFromAttributes(attributes);
		}
	}
}
