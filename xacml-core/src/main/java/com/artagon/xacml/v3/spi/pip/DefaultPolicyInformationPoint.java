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
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.google.common.base.Objects;
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
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);
	
	/**
	 * Resolvers index by category and attribute identifier
	 */
	private Map<AttributeCategoryId, Multimap<String, AttributeResolver>> resolvers;
	
	/**
	 * Resolvers index by policy identifier
	 */
	private Multimap<String, AttributeResolver> resolversByPolicyId;
	
	public DefaultPolicyInformationPoint(){
		this.resolvers = new ConcurrentHashMap<AttributeCategoryId, Multimap<String,AttributeResolver>>();
		this.resolversByPolicyId = HashMultimap.create();
		addResolver(new DefaultEnviromentAttributeResolver());
	}

	@SuppressWarnings("unchecked")
	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeDesignator ref, 
			RequestContextAttributesCallback callback) 
	{
	 	AttributeResolver r = findResolver(context, ref);
	 	return (BagOfAttributeValues<AttributeValue>)((r == null)?ref.getDataType().bagOf().createEmpty():r.resolve(
	 			new DefaultPolicyInformationPointContext(context, ref), ref, callback));
	}

	@Override
	public Node resolve(EvaluationContext context,
			AttributeCategoryId categoryId, RequestContextAttributesCallback callback) {
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
		Multimap<String, AttributeResolver> byCategory = resolvers.get(d.getCategory());
		if(byCategory == null || 
				byCategory.isEmpty()){
			byCategory = HashMultimap.create();
			resolvers.put(d.getCategory(), byCategory);
		}
		for(String attributeId : d.getProvidedAttributeIds())
		{
			boolean changed = byCategory.put(attributeId, resolver);
			if(changed){
				throw new IllegalArgumentException(String.format("AttributeId=\"%s\" for " +
							"category=\"%s\" already provided via other resolver", 
							attributeId, d.getCategory()));
			}
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
	 * @return {@link AttributeResolver} or <code>null</code> if no
	 * resolver found
	 */
	private AttributeResolver findResolver(EvaluationContext context, 
			AttributeDesignator ref)
	{
		if(context == null){
			Multimap<String, AttributeResolver> byCategory = resolvers.get(ref.getCategory());
		 	if(byCategory == null){
		 		return null;
		 	}
		 	Collection<AttributeResolver> resolvers = byCategory.get(ref.getAttributeId());
		 	AttributeResolver found = null;
		 	for(AttributeResolver r : resolvers){
		 		if(Objects.equal(ref.getIssuer(), 
		 				r.getDescriptor().getIssuer())){
		 			found = r;
		 			break;
		 		}
		 	}
		 	return found;
		}
		String policyId = getCurrentIdentifier(context);
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
	
	private String getCurrentIdentifier(EvaluationContext context)
	{
		return (context.getCurrentPolicy() != null)?
			context.getCurrentPolicy().getId():
				(context.getCurrentPolicySet() != null?context.getCurrentPolicySet().getId():null);
	}
}
