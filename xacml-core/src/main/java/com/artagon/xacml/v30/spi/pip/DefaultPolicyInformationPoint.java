package com.artagon.xacml.v30.spi.pip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
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
	
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistry registry;
	
	public DefaultPolicyInformationPoint(ResolverRegistry resolvers, 
			PolicyInformationPointCacheProvider cache)
	{
		Preconditions.checkNotNull(resolvers);
		Preconditions.checkNotNull(cache);
		this.cache = cache;
		this.registry = resolvers;
	}
	
	public DefaultPolicyInformationPoint(ResolverRegistry resolvers){
		this(resolvers, new NoCachePolicyInformationPointCacheProvider());
	}

	@Override
	public BagOfAttributeValues resolve(
			final EvaluationContext context,
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
		ResolverContext pipContext = createContext(context, d);
		AttributeSet attributes = null;
		if(d.isCachable()){
			attributes = cache.getAttributes(pipContext);
			if(attributes != null){
				return attributes.get(ref.getAttributeId());
			}
		}
		attributes = r.resolve(pipContext);
		if(d.isCachable()){
			cache.putAttributes(pipContext, attributes);
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
		ResolverContext pipContext = createContext(context, d);		
		Content v = null;
		if(d.isCachable()){
			v = cache.getContent(pipContext);
			if(v != null){
				return v.getContent();
			}
		}
		v = r.resolve(pipContext);
		if(d.isCachable()){
			cache.putContent(pipContext, v);
		}
		return v.getContent();
	}
	
	private ResolverContext createContext(EvaluationContext context, ResolverDescriptor d) 
		throws EvaluationException
	{
		return new DefaultResolverContext(context, d);
	}
}
