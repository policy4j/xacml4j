package com.artagon.xacml.v3.spi.pip;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.pip.cache.NullCacheProvider;
import com.google.common.base.Preconditions;

/**
 * A default implementation of {@link PolicyInformationPoint}
 * 
 * @author Giedrius Trumpickas
 */
public class DefaultPolicyInformationPoint 
	implements PolicyInformationPoint
{	
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyInformationPoint.class);
	
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
		if(log.isDebugEnabled()){
			log.debug("Trying to resolve " +
					"designator=\"{}\"", ref);
		}
		AttributeResolver r = registry.getAttributeResolver(context, ref);
		if(r == null){
			if(log.isDebugEnabled()){
				log.debug("No matching resolver " +
						"found for designator=\"{}\"", ref);
			}
			return ref.getDataType().emptyBag();
		}
		AttributeResolverDescriptor d = r.getDescriptor();
		Preconditions.checkState(d.canResolve(ref));
		PolicyInformationPointContext pipContext = createContext(context, d);
		List<BagOfAttributeValues> keys = pipContext.getKeys();
		AttributeSet attributes = null;
		if(d.isCachable()){
			attributes = cache.get(d, keys);
			if(attributes != null){
				return attributes.get(ref.getAttributeId());
			}
		}
		attributes = r.resolve(pipContext);
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
		PolicyInformationPointContext pipContext = createContext(context, d);		
		List<BagOfAttributeValues> keys = pipContext.getKeys();
		Node v = null;
		if(d.isCachable()){
			v = cache.get(d, keys);
			if(v != null){
				return v;
			}
		}
		v = r.resolve(pipContext);
		if(d.isCachable()){
			cache.put(d, keys, v);
		}
		return v;
	}
	
	private PolicyInformationPointContext createContext(EvaluationContext context, ResolverDescriptor d)
	{
		return new DefaultPolicyInformationPointContext(context, d);
	}
}
