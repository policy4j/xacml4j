package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public abstract class BaseAttributeResolver implements AttributeResolver
{	
	private final static Logger log = LoggerFactory.getLogger(BaseAttributeResolver.class);
	
	private AttributeResolverDescriptor descriptor;
	
	protected BaseAttributeResolver(AttributeResolverDescriptor descriptor){
		Preconditions.checkNotNull(descriptor);
		this.descriptor = descriptor;
	}
	
	@Override
	public final AttributeResolverDescriptor getDescriptor(){
		return descriptor;
	}

	@Override
	public final Map<String, BagOfAttributeValues> resolve(
			PolicyInformationPointContext context, BagOfAttributeValues ...keys) throws Exception 
	{
		if(log.isDebugEnabled()){
			log.debug("Retrieving attributes via resolver " +
					"id=\"{}\" name=\"{}\"", 
					descriptor.getId(), descriptor.getName());
		}
		Map<String, BagOfAttributeValues> v =  validateResult(doResolve(context, keys));
		if(log.isDebugEnabled()){
			log.debug("Retrieved values=\"{}\"", v);
		}
		return Collections.unmodifiableMap(v);
	}
	
	protected abstract Map<String, BagOfAttributeValues> doResolve(
			PolicyInformationPointContext context,
			BagOfAttributeValues ...keys);
	
	
	private Map<String, BagOfAttributeValues> validateResult(
			Map<String, BagOfAttributeValues> r)
	{
		Map<String, BagOfAttributeValues> result = new HashMap<String, BagOfAttributeValues>(r);
		for(String attributeId : descriptor.getProvidedAttributeIds())
		{
			BagOfAttributeValues v = r.get(attributeId);
			AttributeDescriptor d = descriptor.getAttribute(attributeId);
			Preconditions.checkState(d != null);
			if(v == null){
				result.put(attributeId, d.getDataType().emptyBag());
				continue;
			}
			Preconditions.checkState(d.getDataType().equals(v.getDataType()), 
					"Attribute id=\"%s\" dataType=\"%s\" in result does not match dataType=\"%s\" in descriptor", 
					attributeId, v.getDataType(), d.getDataType());
		}
		return result;
	}
	
	protected final Map<String, BagOfAttributeValues> result(
			String a1, BagOfAttributeValues v1)
	{
		checkAttribute(a1, v1);
		return ImmutableMap.of(a1, v1);
	}
	
	protected final Map<String, BagOfAttributeValues> result(
			String a1, BagOfAttributeValues v1, String a2, BagOfAttributeValues v2)
	{
		checkAttribute(a1, v1);
		checkAttribute(a2, v2);
		return ImmutableMap.of(a1, v1, a2, v2);
	}
	
	protected final Map<String, BagOfAttributeValues> result(
			String a1, BagOfAttributeValues v1, 
			String a2, BagOfAttributeValues v2,
			String a3, BagOfAttributeValues v3)
	{
		checkAttribute(a1, v1);
		checkAttribute(a2, v2);
		checkAttribute(a3, v3);
		return ImmutableMap.of(a1, v1, a2, v2, a3, v3);
	}
	
	protected final Map<String, BagOfAttributeValues> result(
			String a1, BagOfAttributeValues v1, 
			String a2, BagOfAttributeValues v2,
			String a3, BagOfAttributeValues v3,
			String a4, BagOfAttributeValues v4)
	{
		checkAttribute(a1, v1);
		checkAttribute(a2, v2);
		checkAttribute(a3, v3);
		checkAttribute(a4, v4);
		return ImmutableMap.of(a1, v1, a2, v2, a3, v3, a4, v4);
	}
	
	protected final Map<String, BagOfAttributeValues> result(
			String a1, BagOfAttributeValues v1, 
			String a2, BagOfAttributeValues v2,
			String a3, BagOfAttributeValues v3,
			String a4, BagOfAttributeValues v4,
			String a5, BagOfAttributeValues v5)
	{
		checkAttribute(a1, v1);
		checkAttribute(a2, v2);
		checkAttribute(a3, v3);
		checkAttribute(a4, v4);
		checkAttribute(a5, v5);
		return ImmutableMap.of(a1, v1, a2, v2, a3, v3, a4, v4, a5, v5);
	}
	
	private void checkAttribute(String n, BagOfAttributeValues v)
	{
		Preconditions.checkArgument(descriptor.isAttributeProvided(n));
		Preconditions.checkArgument(descriptor.getAttribute(n).getDataType().equals(v.getDataType()));
	}
}
