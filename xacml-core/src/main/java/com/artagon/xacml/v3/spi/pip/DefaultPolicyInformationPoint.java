package com.artagon.xacml.v3.spi.pip;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.google.common.base.Preconditions;
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
	private Map<AttributeCategory, Map<String, AttributeResolver>> attributeResolvers;
	
	/**
	 * Resolvers index by policy identifier
	 */
	private Multimap<String, AttributeResolver> attributeResolversByPolicy;
	
	
	private Map<AttributeCategory, ContentResolver> contentResolvers;
	private Multimap<String, ContentResolver> contentResolversByPolicy;
	
	
	public DefaultPolicyInformationPoint(){
		this.attributeResolvers = new ConcurrentHashMap<AttributeCategory, Map<String,AttributeResolver>>();
		this.attributeResolversByPolicy = HashMultimap.create();
		this.contentResolversByPolicy = HashMultimap.create();
		this.contentResolvers = new ConcurrentHashMap<AttributeCategory, ContentResolver>();
		addResolver(AnnotatedAttributeResolver.create(new DefaultEnviromentAttributeResolver()));
	}

	@Override
	public BagOfAttributeValues resolve(
			EvaluationContext context,
			AttributeDesignator ref, 
			RequestContextCallback callback) 
				throws EvaluationException 
	{
	 	AttributeResolver r = findResolver(context, ref);
	 	try{
	 		if(r == null){
	 			throw new AttributeReferenceEvaluationException(context, ref, 
	 					"No resolver is found, to resolve given reference");
	 		}
	 		return r.resolve(new DefaultPolicyInformationPointContext(context, 
	 				callback, ref.getCategory()), 
		 						ref.getCategory(), 
		 			 			ref.getAttributeId(), 
		 			 			ref.getDataType(),
		 			 			ref.getIssuer());
	 	}catch(AttributeReferenceEvaluationException e){
	 		throw e;
	 	}catch(Exception e){
	 		throw new AttributeReferenceEvaluationException(
	 				context, ref, StatusCode.createMissingAttribute() , e);
	 	}
	}

	@Override
	public Node resolve(EvaluationContext context,
			AttributeCategory category, RequestContextCallback callback) throws EvaluationException
	{
		ContentResolver r = findContentResolver(context, category);
		return r != null?r.getContent(category, 
				new DefaultPolicyInformationPointContext(context, callback, category)):null;
	} 
	
	
	private void addResolverForCategory(AttributeCategory category, AttributeResolver resolver)
	{
		AttributeResolverDescriptor d = resolver.getDescriptor();
		for(AttributeCategory c : d.getSupportedCategores())
		{
			Map<String, AttributeResolver> byCategory = attributeResolvers.get(category);
			if(byCategory == null){
				byCategory = new ConcurrentHashMap<String, AttributeResolver>();
				attributeResolvers.put(c, byCategory);
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
		for(AttributeCategory category : d.getSupportedCategores()){
			addResolverForCategory(category, resolver);
		}
	}
	
	public void addResolver(ContentResolver r)
	{
		Preconditions.checkArgument(r != null);
		ContentResolverDescriptor d = r.getDescriptor();
		for(AttributeCategory category : d.getSupportedCategories()){
			contentResolvers.put(category, r);
		}
	}
	
	public void addResolver(String policyId, ContentResolver r){
		Preconditions.checkArgument(r != null);
		this.contentResolversByPolicy.put(policyId, r);
	}
	
	/**
	 * Adds resolver for specific policy or policy set
	 * and policies down the evaluation tree
	 * 
	 * @param policyId a policy identifier
	 * @param resolver an attribute resolver
	 */
	public void addResolver(String policyId, AttributeResolver resolver){
		this.attributeResolversByPolicy.put(policyId, resolver);
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
		// stop recursive call if 
		// context is null
		if(context == null)
		{
			Map<String, AttributeResolver> byCategory = attributeResolvers.get(ref.getCategory());
		 	if(byCategory == null){
		 		return null;
		 	}
		 	AttributeResolver resolver = byCategory.get(ref.getAttributeId());
		 	return (resolver != null && resolver.getDescriptor().canResolve(
		 			ref.getCategory(), 
		 			ref.getAttributeId(), 
		 			ref.getDataType(),
		 			ref.getIssuer()))?resolver:null;
		}
		String policyId = getCurrentIdentifier(context);
		Collection<AttributeResolver> found = attributeResolversByPolicy.get(policyId);
		if(log.isDebugEnabled()){
			log.debug("Found \"{}\" resolver " +
					"scoped for a PolicyId=\"{}\"", 
					found.size(), policyId);
		}
		for(AttributeResolver r : found)
		{
			AttributeResolverDescriptor d = r.getDescriptor();
			if(d.canResolve(ref.getCategory(), 
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
	
	private ContentResolver findContentResolver(EvaluationContext context, 
			AttributeCategory category)
	{
		// stop recursive call if 
		// context is null
		if(context == null){
			return contentResolvers.get(category);
		}
		String policyId = getCurrentIdentifier(context);
		Collection<ContentResolver> found = contentResolversByPolicy.get(policyId);
		if(log.isDebugEnabled()){
			log.debug("Found \"{}\" resolver " +
					"scoped for a PolicyId=\"{}\"", 
					found.size(), policyId);
		}
		for(ContentResolver r : found)
		{
			ContentResolverDescriptor d = r.getDescriptor();
			if(d.canResolve(category)){
				if(log.isDebugEnabled()){
					log.debug("Found PolicyId=\"{}\" scoped resolver", policyId);
				}
				return r;
			}
		}
		return findContentResolver(context.getParentContext(), category);
	}
	
	private String getCurrentIdentifier(EvaluationContext context)
	{
		Policy currentPolicy = context.getCurrentPolicy();
		if(currentPolicy == null){
			PolicySet currentPolicySet = context.getCurrentPolicySet();
			return currentPolicySet != null?currentPolicySet.getId():null;
		}
		return (currentPolicy != null)?currentPolicy.getId():null;		
	}
}
