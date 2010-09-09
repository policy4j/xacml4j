package com.artagon.xacml.v3.spi.pip;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class AttributeResolverDescriptorAdapter extends BaseAttributeResolver
{
	private Object instance;
	private Map<AttributeCategoryId, Map<String, Method>> resolvers;
	
	public AttributeResolverDescriptorAdapter(
			AttributeResolverDescriptor descriptor, 
			Map<AttributeCategoryId, Map<String, Method>> methods, Object instance) 
	{
		super(descriptor);
		Preconditions.checkArgument(instance != null);
		this.instance = instance;
		this.resolvers = new HashMap<AttributeCategoryId, Map<String,Method>>();
		for(AttributeCategoryId c : methods.keySet()){
			resolvers.put(c, ImmutableMap.copyOf(methods.get(c)));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected BagOfAttributeValues<AttributeValue> doResolve(
			PolicyInformationPointContext context, AttributeDesignator ref,
			RequestContextAttributesCallback callback) 
				throws Exception 
	{
		Map<String, Method> byId = resolvers.get(ref.getCategory());
		Preconditions.checkState(byId != null);
		Method m = byId.get(ref.getAttributeId());
		Preconditions.checkState(m != null);
		return (BagOfAttributeValues<AttributeValue>)m.invoke(instance, new Object[]{context, callback});
		
	}	
	

}
