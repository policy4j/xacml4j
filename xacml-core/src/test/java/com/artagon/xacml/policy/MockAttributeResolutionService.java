package com.artagon.xacml.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

import com.artagon.xacml.CategoryId;



public class MockAttributeResolutionService implements AttributeResolutionService
{
	private Map<CategoryId, Map<String, AttributeHolder>> attributes;
	
	public MockAttributeResolutionService(){
		this.attributes = new HashMap<CategoryId, Map<String,AttributeHolder>>();
	}
	
	@Override
	public BagOfAttributes<?> resolve(
			CategoryId category,
			String attributeId,
			AttributeType dataType, String issuer) 
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
	public BagOfAttributes<?> resolve(CategoryId category, XPath location,
			AttributeType dataType) {
		return dataType.bagOf().createEmpty();
	}
	
	public void addAttribute(
			CategoryId category, 
			String attributeId, 
			String issuer,
			AttributeType type, 
			Collection<AttributeValue> attributeValues){
		AttributeHolder holder = new AttributeHolder(issuer, category, type, attributeValues);
		Map<String, AttributeHolder> byCategory = attributes.get(category);
		if(byCategory == null){
			byCategory = new HashMap<String, AttributeHolder>();
			attributes.put(category, byCategory);
		}
		byCategory.put(attributeId, holder);
		
	}
	
	class AttributeHolder
	{
		String issuer;
		BagOfAttributes<?> bag;
		CategoryId category;
		
		public AttributeHolder(
				String issuer, 
				CategoryId category,
				AttributeType type, Collection<AttributeValue> attributes) 
		{
			this.issuer = issuer;
			this.category = category;
			this.bag = type.bagOf().createFromAttributes(attributes);
		}
	}
}
