package com.artagon.xacml.v3.spi;

import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestAttributesCallback;

public class DefaultPolicyInformationPoint implements PolicyInformationPoint
{
	private Map<AttributeCategoryId, Map<String, AttributeResolver>> registry;
	
	public DefaultPolicyInformationPoint(){
		this.registry = new ConcurrentHashMap<AttributeCategoryId, Map<String,AttributeResolver>>();
	}

	@Override
	public BagOfAttributeValues<? extends AttributeValue> resolve(
			AttributeDesignator ref, RequestAttributesCallback callback) 
	{
	 	Map<String, AttributeResolver> byCategory = registry.get(ref.getCategory());
	 	if(byCategory == null){
	 		return ref.getDataType().bagOf().createEmpty();
	 	}
	 	AttributeResolver r = byCategory.get(ref.getAttributeId());
	 	return (r == null)?ref.getDataType().bagOf().createEmpty():r.resolve(ref, callback);
	} 
}
