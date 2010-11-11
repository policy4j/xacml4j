package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.google.common.base.Preconditions;

/**
 * A default implementation of {@link PolicyInformationPoint}
 * 
 * @author Giedrius Trumpickas
 */
public class DefaultPolicyInformationPoint 
	implements PolicyInformationPoint
{	
	private ResolverResultCacheProvider cache;
	private ResolverRegistry registry;
	
	public DefaultPolicyInformationPoint(ResolverRegistry resolvers, 
			ResolverResultCacheProvider cache)
	{
		Preconditions.checkNotNull(cache);
		Preconditions.checkNotNull(resolvers);
		this.cache = cache;
		this.registry = resolvers;
	}
	
	public DefaultPolicyInformationPoint(ResolverRegistry resolvers){
		this(resolvers, new NullCacheProvider());
	}

	@Override
	public BagOfAttributeValues resolve(final EvaluationContext context,
			AttributeDesignatorKey ref) throws Exception 
	{
		AttributeResolver r = registry.getAttributeResolver(context, ref);
		if(r == null){
			return ref.getDataType().emptyBag();
		}
		AttributeResolverDescriptor d = r.getDescriptor();
		Preconditions.checkState(d.canResolve(ref));
		BagOfAttributeValues[] keys = d.resolveKeys(context);
		AttributeSet attributes = null;
		if(d.isCachable()){
			attributes = cache.get(d, keys);
			if(attributes != null){
				return attributes.get(ref.getAttributeId());
			}
		}
		attributes = r.resolve(
				new DefaultPolicyInformationPointContext(context, d, keys));
		if(d.isCachable()){
			cache.put(d, keys, attributes);
		}
		return attributes.get(ref.getAttributeId());
	}

	@Override
	public Node resolve(final EvaluationContext context, 
			AttributeCategory category)
			throws Exception 
	{
		ContentResolver r = registry.getContentResolver(context, category);
		if(r == null){
			return null;
		}
		ContentResolverDescriptor d = r.getDescriptor();
		BagOfAttributeValues[] keys = d.resolveKeys(context);
		Node v = null;
		if(d.isCachable()){
			v = cache.get(d, keys);
			if(v != null){
				return v;
			}
		}
		v = r.resolve(
				new DefaultPolicyInformationPointContext(context, d, keys));
		if(d.isCachable()){
			cache.put(d, keys, v);
		}
		return v;
	}
}
