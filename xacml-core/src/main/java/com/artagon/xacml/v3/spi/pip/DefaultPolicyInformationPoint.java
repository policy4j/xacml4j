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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class DefaultPolicyInformationPoint 
	implements PolicyInformationPoint
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);
	
	/**
	 * Resolvers index by category and attribute identifier
	 */
	private Map<AttributeCategoryId, Map<String, AttributeResolver>> resolvers;
	
	/**
	 * Resolvers index by policy identifier
	 */
	private Multimap<String, AttributeResolver> resolversByPolicyId;
	
	public DefaultPolicyInformationPoint(){
		this.resolvers = new ConcurrentHashMap<AttributeCategoryId, Map<String,AttributeResolver>>();
		this.resolversByPolicyId = HashMultimap.create();
		addResolver(new DefaultEnviromentAttributeResolver());
	}

	@SuppressWarnings("unchecked")
	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeDesignator ref, 
			RequestAttributesCallback callback) 
	{
	 	AttributeResolver r = findResolver(context, ref);
	 	return (BagOfAttributeValues<AttributeValue>)((r == null)?ref.getDataType().bagOf().createEmpty():r.resolve(
	 			new DefaultPolicyInformationPointContext(context), ref, callback));
	}

	@Override
	public Node resolve(EvaluationContext context,
			AttributeCategoryId categoryId, RequestAttributesCallback callback) {
		return null;
	} 
	
	/**
	 * Adds new attribute resolver
	 * 
	 * @param resolver an attribute resolver
	 */
	public void addResolver(AttributeResolver resolver)
	{
		Preconditions.checkNotNull(resolver);
		AttributeResolverDescriptor d = resolver.getDescriptor();
		Map<String, AttributeResolver> byCategory = resolvers.get(d.getCategory());
		if(byCategory == null || 
				byCategory.isEmpty()){
			byCategory = new ConcurrentHashMap<String, AttributeResolver>();
			resolvers.put(d.getCategory(), byCategory);
		}
		for(String attributeId : d.getProvidedAttributes()){
				if(log.isDebugEnabled()){
					log.debug("Adding resolver for category=\"{}\" " +
							"attributeId=\"{}\"", 
							d.getCategory(), attributeId);
		}
		AttributeResolver oldResolver = byCategory.put(attributeId, resolver);
		if(oldResolver != null){
			throw new IllegalArgumentException(String.format("AttributeId=\"%s\" for " +
							"category=\"%s\" already provided via other resolver", 
							attributeId, d.getCategory()));
			}
		}
	}
	
	/**
	 * Adds resolver for specific policy or policy set
	 * 
	 * @param policyId a policy identifier
	 * @param resolver an attribute resolver
	 */
	public void addResolver(String policyId, AttributeResolver resolver){
		this.resolversByPolicyId.put(policyId, resolver);
	}
	
	/**
	 * Finds {@link AttributeResolver} for given evaluation context and
	 * {@link AttributeResolver} instance
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute reference
	 * @return {@link AttributeResolver} or <code>null</code> if no
	 * resolver found
	 */
	private AttributeResolver findResolver(EvaluationContext context, 
			AttributeDesignator ref)
	{
		if(context == null){
			Map<String, AttributeResolver> byCategory = resolvers.get(ref.getCategory());
		 	if(byCategory == null){
		 		return null;
		 	}
		 	return byCategory.get(ref.getAttributeId());
		}
		String policyId = (context.getCurrentPolicy() != null)?
				context.getCurrentPolicy().getId():
					(context.getCurrentPolicySet() != null?context.getCurrentPolicySet().getId():null);
		if(log.isDebugEnabled()){
			log.debug("Trying to locate attribute " +
					"resolvers for PolicyId=\"{}\"", policyId);
		}
		Collection<AttributeResolver> found = resolversByPolicyId.get(policyId);
		if(found.isEmpty()){
			return findResolver(context.getParentContext(), ref);
		}
		for(AttributeResolver r : found){
			if(r.canResolve(ref)){
				return r;
			}
		}
		return findResolver(context.getParentContext(), ref);
	}
}
