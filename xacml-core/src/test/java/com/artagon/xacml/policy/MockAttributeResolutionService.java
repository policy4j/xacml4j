package com.artagon.xacml.policy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;

import org.oasis.xacml.azapi.constants.AzCategoryId;



public class MockAttributeResolutionService implements AttributeResolutionService
{
	private Map<AzCategoryId, Map<String, AttributeHolder>> attributes;
	
	public MockAttributeResolutionService(){
		this.attributes = new HashMap<AzCategoryId, Map<String,AttributeHolder>>();
	}
	
	@Override
	public BagOfAttributes<?> resolve(
			AzCategoryId category,
			String attributeId,
			AttributeDataType dataType, String issuer) 
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
	public BagOfAttributes<?> resolve(AzCategoryId category, XPath location,
			AttributeDataType dataType) {
		return dataType.bagOf().createEmpty();
	}
	
	public void addAttribute(
			AzCategoryId category, 
			String attributeId, 
			String issuer,
			AttributeDataType type, 
			Collection<Attribute> attributeValues){
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
		AzCategoryId category;
		
		public AttributeHolder(
				String issuer, 
				AzCategoryId category,
				AttributeDataType type, Collection<Attribute> attributes) 
		{
			this.issuer = issuer;
			this.category = category;
			this.bag = type.bagOf().createFromAttributes(attributes);
		}
	}
}
