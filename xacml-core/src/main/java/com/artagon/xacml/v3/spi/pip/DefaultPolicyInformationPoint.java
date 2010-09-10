package com.artagon.xacml.v3.spi.pip;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.artagon.xacml.v3.sdk.AnnotatedAttributeResolver;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A default implementation of {@link PolicyInformationPoint}
 * 
 * @author Giedrius Trumpickas
 */
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
		addResolver(AnnotatedAttributeResolver.create(new DefaultEnviromentAttributeResolver()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeDesignator ref, 
			RequestContextAttributesCallback callback) 
				throws AttributeReferenceEvaluationException 
	{
	 	AttributeResolver r = findResolver(context, ref);
	 	try{
	 		return (BagOfAttributeValues<AttributeValue>)((r == null)?
		 			ref.getDataType().bagOf().createEmpty():
		 				r.resolve(
		 						new DefaultPolicyInformationPointContext(context, callback, ref), 
		 						ref.getCategory(), 
		 			 			ref.getAttributeId(), 
		 			 			ref.getDataType(),
		 			 			ref.getIssuer()));
	 	}catch(Exception e){
	 		throw new AttributeReferenceEvaluationException(context, ref, e);
	 	}
	}

	@Override
	public Node resolve(EvaluationContext context,
			AttributeCategoryId categoryId, RequestContextAttributesCallback callback) {
		return null;
	} 
	
	
	private void addResolverForCategory(AttributeCategoryId category, AttributeResolver resolver)
	{
		AttributeResolverDescriptor d = resolver.getDescriptor();
		for(AttributeCategoryId c : d.getSupportedCategores())
		{
			Map<String, AttributeResolver> byCategory = resolvers.get(category);
			if(byCategory == null){
				byCategory = new ConcurrentHashMap<String, AttributeResolver>();
				resolvers.put(c, byCategory);
			}
			for(String attributeId : d.getProvidedAttributeIds(c))
			{
				if(log.isDebugEnabled()){
					log.debug("Adding resolver for category=\"{}\", " +
							"attributeId=\"{}\"", category, attributeId);
				}
				AttributeResolver oldResolver = byCategory.get(attributeId);
				if(oldResolver != null){
					throw new IllegalArgumentException(String.format("AttributeId=\"%s\" for " +
								"category=\"%s\" already provided via other resolver", attributeId, category));
				}
				byCategory.put(attributeId, resolver);
			}
		}
		

	}
	
	/**
	 * Adds new attribute resolver
	 * 
	 * @param resolver an attribute resolver
	 */
	public void addResolver(AttributeResolver resolver)
	{
		AttributeResolverDescriptor d = resolver.getDescriptor();
		for(AttributeCategoryId category : d.getSupportedCategores()){
			addResolverForCategory(category, resolver);
		}
	}
	
	/**
	 * Adds resolver for specific policy or policy set
	 * and policies down the evaluation tree
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
	 * @return {@link AttributeResolver} or <code>null</code> if 
	 * no matching resolver found
	 */
	private AttributeResolver findResolver(EvaluationContext context, 
			AttributeDesignator ref)
	{
		if(context == null)
		{
			Map<String, AttributeResolver> byCategory = resolvers.get(ref.getCategory());
		 	if(byCategory == null){
		 		return null;
		 	}
		 	AttributeResolver resolver = byCategory.get(ref.getAttributeId());
		 	return (resolver != null && resolver.canResolve(
		 			ref.getCategory(), 
		 			ref.getAttributeId(), 
		 			ref.getDataType(),
		 			ref.getIssuer()))?resolver:null;
		}
		String policyId = getCurrentIdentifier(context);
		Collection<AttributeResolver> found = resolversByPolicyId.get(policyId);
		if(log.isDebugEnabled()){
			log.debug("Found \"{}\" resolver " +
					"scoped for a PolicyId=\"{}\"", 
					found.size(), policyId);
		}
		for(AttributeResolver r : found){
			if(r.canResolve(ref.getCategory(), 
		 			ref.getAttributeId(), 
		 			ref.getDataType(),
		 			ref.getIssuer())){
				if(log.isDebugEnabled()){
					log.debug("Found PolicyId=\"{}\" scoped resolver", policyId);
				}
				return r;
			}
		}
		return findResolver(context.getParentContext(), ref);
	}
	
	private String getCurrentIdentifier(EvaluationContext context)
	{
		return (context.getCurrentPolicy() != null)?
			context.getCurrentPolicy().getId():
				(context.getCurrentPolicySet() != null?context.getCurrentPolicySet().getId():null);
	}
}
