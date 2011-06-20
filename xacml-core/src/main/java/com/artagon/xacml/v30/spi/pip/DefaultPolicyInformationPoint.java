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
	
	private String id;
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistry registry;
	
	public DefaultPolicyInformationPoint(String id, 
			ResolverRegistry resolvers, 
			PolicyInformationPointCacheProvider cache)
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(resolvers);
		Preconditions.checkNotNull(cache);
		this.id = id;
		this.cache = cache;
		this.registry = resolvers;
	}
	
	public DefaultPolicyInformationPoint(String id, ResolverRegistry resolvers){
		this(id, resolvers, new NoCachePolicyInformationPointCacheProvider());
	}

	public String getId(){
		return id;
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
				if(log.isDebugEnabled()){
					log.debug("Found cached " +
							"attribute values=\"{}\"", attributes);
				}
				return attributes.get(ref.getAttributeId());
			}
		}
		try{
			attributes = r.resolve(pipContext);
			if(log.isDebugEnabled()){
				log.debug("Resolved attributes=\"{}\"", 
						attributes);
			}
		}catch(Exception e){
			return ref.getDataType().emptyBag();
		}
		// cache values to the context
		for(AttributeDesignatorKey k : attributes.getAttributeKeys()){
			context.setDesignatorValue(k, attributes.get(k));
		}
		// check if resolver
		// descriptor allows long term caching
		if(d.isCachable()){
			cache.putAttributes(pipContext, attributes);
		}
		context.setDecisionCacheTTL(d.getPreferreredCacheTTL());
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
		try{
			v = r.resolve(pipContext);
		}catch(Exception e){
			return null;
		}
		if(d.isCachable()){
			cache.putContent(pipContext, v);
		}
		context.setDecisionCacheTTL(d.getPreferreredCacheTTL());
		return v.getContent();
	}
	
	private ResolverContext createContext(EvaluationContext context, ResolverDescriptor d) 
		throws EvaluationException
	{
		return new DefaultResolverContext(context, d);
	}
}
