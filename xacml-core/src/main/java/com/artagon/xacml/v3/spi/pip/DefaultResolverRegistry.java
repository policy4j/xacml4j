package com.artagon.xacml.v3.spi.pip;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class DefaultResolverRegistry implements ResolverRegistry
{
	private final static Logger log = LoggerFactory.getLogger(DefaultResolverRegistry.class);
	
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
	
	private Map<String, AttributeResolver> attributeResolversById;
	private Map<String, AttributeResolver> contentResolversById;
	
	public DefaultResolverRegistry()
	{
		addResolver(new DefaultEnviromentAttributeResolver());
		this.attributeResolvers = new ConcurrentHashMap<AttributeCategory, Map<String,AttributeResolver>>();
		this.attributeResolversByPolicy = HashMultimap.create();
		this.contentResolvers = new ConcurrentHashMap<AttributeCategory, ContentResolver>();
		this.contentResolversByPolicy = HashMultimap.create();
		this.attributeResolversById = new ConcurrentHashMap<String, AttributeResolver>();
		this.contentResolversById = new ConcurrentHashMap<String, AttributeResolver>();
	}
	
	public void addResolver(AttributeResolver resolver)
	{
		AttributeResolverDescriptor d = resolver.getDescriptor();
		Preconditions.checkState(!attributeResolversById.containsKey(d.getId()));
		Map<String, AttributeResolver> byCategory = attributeResolvers.get(d.getCategory());
		if(byCategory == null){
			byCategory = new ConcurrentHashMap<String, AttributeResolver>();
			attributeResolvers.put(d.getCategory(), byCategory);
		}
		for(String attributeId : d.getProvidedAttributeIds())
		{
			if(log.isDebugEnabled()){
					log.debug("Adding resolver for category=\"{}\", " +
							"attributeId=\"{}\"", d.getCategory(), attributeId);
			}
			AttributeResolver oldResolver = byCategory.get(attributeId);
				if(oldResolver != null){
					throw new IllegalArgumentException(String.format("AttributeId=\"%s\" for " +
								"category=\"%s\" already provided via other resolver", 
								attributeId, d.getCategory()));
				}
			byCategory.put(attributeId, resolver);
		}
	}
	
	
	public void addResolver(ContentResolver r)
	{
		Preconditions.checkArgument(r != null);
		Preconditions.checkState(!contentResolversById.containsKey(r.getDescriptor().getId()));
		ContentResolverDescriptor d = r.getDescriptor();
		contentResolvers.put(d.getCategory(), r);
	}
	
	public void addResolver(String policyId, ContentResolver r)
	{
		Preconditions.checkArgument(r != null);
		Preconditions.checkState(!contentResolversById.containsKey(r.getDescriptor().getId()));
		this.contentResolversByPolicy.put(policyId, r);
	}
	
	public void addResolver(String policyId, AttributeResolver r)
	{
		AttributeResolverDescriptor d = r.getDescriptor();
		Preconditions.checkState(!attributeResolversById.containsKey(d.getId()));
		this.attributeResolversByPolicy.put(policyId, r);
		this.attributeResolversById.put(d.getId(), r);
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
	public AttributeResolver getAttributeResolver(
			EvaluationContext context, 
			AttributeDesignatorKey ref)
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
		 	AttributeResolverDescriptor d = resolver.getDescriptor();
		 	return (resolver != null && d.canResolve(ref))?resolver:null;
		}
		String policyId = getCurrentIdentifier(context);
		Collection<AttributeResolver> found = attributeResolversByPolicy.get(policyId);
		if(log.isDebugEnabled()){
			log.debug("Found \"{}\" resolver " +
					"scoped for a PolicyId=\"{}\"", 
					found.size(), policyId);
		}
		for(AttributeResolver r : found){
			AttributeResolverDescriptor d = r.getDescriptor();
			if(d.canResolve(ref)){
				if(log.isDebugEnabled()){
					log.debug("Found PolicyId=\"{}\" scoped resolver", policyId);
				}
				return r;
			}
		}
		return getAttributeResolver(
				context.getParentContext(), ref);
	}
	
	/**
	 * Gets matching content resolver for a given
	 * evaluation context and attribute category
	 * 
	 * @param context an evaluation context
	 * @param category an attribute category
	 * @return {@link ContentResolver} or <code>null</code>
	 * 
	 */
	public ContentResolver getContentResolver(EvaluationContext context, 
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
		return getContentResolver(context.getParentContext(), category);
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
