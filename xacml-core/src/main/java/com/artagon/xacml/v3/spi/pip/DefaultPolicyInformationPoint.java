package com.artagon.xacml.v3.spi.pip;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestAttributesCallback;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.google.common.base.Preconditions;

public class DefaultPolicyInformationPoint implements PolicyInformationPoint
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);
	
	private Map<AttributeCategoryId, Map<String, AttributeResolver>> registry;
	
	public DefaultPolicyInformationPoint(){
		this.registry = new ConcurrentHashMap<AttributeCategoryId, Map<String,AttributeResolver>>();
		addResolver(new DefaultEnviromentAttributeResolver());
	}

	@SuppressWarnings("unchecked")
	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeDesignator ref, 
			RequestAttributesCallback callback) 
	{
	 	Map<String, AttributeResolver> byCategory = registry.get(ref.getCategory());
	 	if(byCategory == null){
	 		return (BagOfAttributeValues<AttributeValue>)ref.getDataType().bagOf().createEmpty();
	 	}
	 	AttributeResolver r = byCategory.get(ref.getAttributeId());
	 	if(log.isDebugEnabled()){
	 		log.debug("Found resolver attribute reference=\"{}\"", ref);
	 	}
	 	return (BagOfAttributeValues<AttributeValue>)((r == null)?ref.getDataType().bagOf().createEmpty():r.resolve(
	 			new DefaultPolicyInformationPointContext(context), ref, callback));
	}

	@Override
	public Node resolve(EvaluationContext context,
			AttributeCategoryId categoryId, RequestAttributesCallback callback) {
		return null;
	} 
	
	public void setResolvers(Collection<AttributeResolver> resolvers)
	{
		for(AttributeResolver r : resolvers){
			addResolver(r);
		}
	}
	
	public void addResolver(AttributeResolver resolver)
	{
		Preconditions.checkNotNull(resolver);
		AttributeResolverDescriptor d = resolver.getDescriptor();
		for(AttributeCategoryId c : d.getProvidedCategories())
		{
			Map<String, AttributeResolver> byCategory = registry.get(c);
			if(byCategory == null){
				byCategory = new ConcurrentHashMap<String, AttributeResolver>();
				registry.put(c, byCategory);
			}
			for(String attributeId : d.getProvidedAttributes(c)){
				if(log.isDebugEnabled()){
					log.debug("Adding resolver for category=\"{}\" " +
							"attributeId=\"{}\"", c, attributeId);
				}
				AttributeResolver oldResolver = byCategory.put(attributeId, resolver);
				if(oldResolver != null){
					throw new IllegalArgumentException(String.format("AttributeId=\"%s\" for " +
							"category=\"%s\" already provided via other resolver", attributeId, c));
				}
			}
		}
	}
}
