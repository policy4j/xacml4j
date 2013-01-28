package org.xacml4j.v30.spi.pip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

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

	private Timer attrResTimer;
	private Timer contResTimer;
	
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
		this.attrResTimer = Metrics.newTimer(DefaultPolicyInformationPoint.class, "attribute-resolve", id);
		this.contResTimer = Metrics.newTimer(DefaultPolicyInformationPoint.class, "content-resolve", id);
	}
	
	public String getId(){
		return id;
	}

	@Override
	public BagOfAttributeExp resolve(
			final EvaluationContext context,
			AttributeDesignatorKey ref) throws Exception
	{
		TimerContext timerContext = attrResTimer.time();
		try{
			if(log.isDebugEnabled()){
				log.debug("Trying to resolve " +
						"designator=\"{}\"", ref);
			}
			Iterable<AttributeResolver> resolvers = registry.getMatchingAttributeResolvers(context, ref);
			if(Iterables.isEmpty(resolvers)){
				if(log.isDebugEnabled()){
					log.debug("No matching resolver " +
							"found for designator=\"{}\"", ref);
				}
				return ref.getDataType().emptyBag();
			}
			for(AttributeResolver r : resolvers)
			{
				AttributeResolverDescriptor d = r.getDescriptor();
				Preconditions.checkState(d.canResolve(ref));
				ResolverContext rContext = createContext(context, d);
				AttributeSet attributes = null;
				if(d.isCachable()){
					if(log.isDebugEnabled()){
						log.debug("Trying to find resolver id=\"{}\" " +
								"values in cache", d.getId());
					}
					attributes = cache.getAttributes(rContext);
					if(attributes != null && 
							!isExpired(attributes, context)){
						if(log.isDebugEnabled()){
							log.debug("Found cached resolver id=\"{}\"" +
									" values=\"{}\"", d.getId(), attributes);
						}
						return attributes.get(ref.getAttributeId());
					}
				}
				try{
					if(log.isDebugEnabled()){
						log.debug("Trying to resolve values with " +
								"resolver id=\"{}\"", d.getId());
					}
					attributes = r.resolve(rContext);
					if(attributes.isEmpty()){
						if(log.isDebugEnabled()){
							log.debug("Resolver id=\"{}\" failed " +
									"to resolve attributes", d.getId());
						}
						continue;
					}
					if(log.isDebugEnabled()){
						log.debug("Resolved attributes=\"{}\"",
								attributes);
					}
				}catch(Exception e){
					if(log.isDebugEnabled()){
						log.debug("Resolver id=\"{}\" failed " +
									"to resolve attributes", d.getId());
						log.debug("Resolver threw an exception", e);
					}
					continue;
				}
				// cache values to the context
				for(AttributeDesignatorKey k : attributes.getAttributeKeys()){
					BagOfAttributeExp v = attributes.get(k);
					if(log.isDebugEnabled()){
						log.debug("Caching desginator=\"{}\" " +
								"value=\"{}\" to context", k, v);
					}
					context.setResolvedDesignatorValue(k, v);
				}
				// check if resolver
				// descriptor allows long term caching
				if(d.isCachable()){
					cache.putAttributes(rContext, attributes);
				}
				context.setDecisionCacheTTL(d.getPreferreredCacheTTL());
				return attributes.get(ref.getAttributeId());
			}
			return ref.getDataType().emptyBag();
		}finally{
			timerContext.stop();
		}
	}
	
	private boolean isExpired(AttributeSet v, EvaluationContext context){
		return ((context.getTicker().read() - v.getTimestamp()) / 
				1000000000L) >= v.getDescriptor().getPreferreredCacheTTL();
	}
	
	private boolean isExpired(Content v, EvaluationContext context){
		return ((context.getTicker().read() - v.getTimestamp()) / 
				1000000000L) >= v.getDescriptor().getPreferreredCacheTTL();
	}

	@Override
	public Node resolve(final EvaluationContext context,
			AttributeCategory category)
			throws Exception
	{
		TimerContext timerContext = contResTimer.time();
		try{
			ContentResolver r = registry.getMatchingContentResolver(context, category);
			if(r == null){
				return null;
			}
			ContentResolverDescriptor d = r.getDescriptor();
			ResolverContext pipContext = createContext(context, d);
			Content v = null;
			if(d.isCachable()){
				v = cache.getContent(pipContext);
				if(v != null && 
						!isExpired(v, context)){
					if(log.isDebugEnabled()){
						log.debug("Found cached " +
								"content=\"{}\"", v);
					}
					return v.getContent();
				}
			}
			try{
				v = r.resolve(pipContext);
			}catch(Exception e){
				if(log.isDebugEnabled()){
					log.debug("Received error=\"{}\" " +
							"while resolving content for category=\"{}\"",
							e.getMessage(), category);
					log.debug("Error stack trace", e);
				}
				return null;
			}
			if(d.isCachable()){
				cache.putContent(pipContext, v);
			}
			context.setDecisionCacheTTL(d.getPreferreredCacheTTL());
			return v.getContent();
		}finally{
			timerContext.stop();
		}

	}

	@Override
	public ResolverRegistry getRegistry() {
		return registry;
	}

	private ResolverContext createContext(EvaluationContext context, ResolverDescriptor d)
		throws EvaluationException
	{
		return new DefaultResolverContext(context, d);
	}
}
