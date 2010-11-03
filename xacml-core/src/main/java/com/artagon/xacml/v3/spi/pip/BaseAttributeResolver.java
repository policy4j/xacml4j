package com.artagon.xacml.v3.spi.pip;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.google.common.base.Preconditions;

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
	
	protected final ResultBuilder newResult(){
		return new ResultBuilder();
	}
	
	public class ResultBuilder
	{
		private Map<String, BagOfAttributeValues> values;
		
		private ResultBuilder(){
			this.values = new HashMap<String, BagOfAttributeValues>();
		}
		
		public ResultBuilder value(String attributeId, BagOfAttributeValues v)
		{
			Preconditions.checkArgument(descriptor.isAttributeProvided(attributeId));
			Preconditions.checkArgument(descriptor.getAttribute(attributeId).getDataType().equals(v.getDataType()));
			values.put(attributeId, v);
			return this;
		}
		
		public Map<String, BagOfAttributeValues> build(){
			return values;
		}
	}
}
