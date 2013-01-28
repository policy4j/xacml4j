package com.artagon.xacml.v30.spi.pip;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.BagOfAttributeExp;
import com.google.common.base.Preconditions;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * A base implementation of {@link AttributeResolver}
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeResolver implements AttributeResolver
{	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private AttributeResolverDescriptorDelegate descriptor;
	
	private Counter failuresCount;
	private Timer attrResolveTimer;
	private AtomicInteger preferedCacheTTL;
	
	protected BaseAttributeResolver(
			AttributeResolverDescriptor descriptor){
		Preconditions.checkNotNull(descriptor);
		this.descriptor = new AttributeResolverDescriptorDelegate(descriptor){
			@Override
			public int getPreferreredCacheTTL() {
				return (preferedCacheTTL == null)?
						super.getPreferreredCacheTTL():preferedCacheTTL.get();
			}
		};
		this.attrResolveTimer = Metrics.newTimer(BaseAttributeResolver.class, "attributes-resolve", descriptor.getId());
		this.failuresCount = Metrics.newCounter(BaseAttributeResolver.class, "failures-count", descriptor.getId());
	}
	
	@Override
	public final AttributeResolverDescriptor getDescriptor(){
		return descriptor;
	}

	@Override
	public final AttributeSet resolve(
			ResolverContext context) throws Exception 
	{
		Preconditions.checkArgument(
				context.getDescriptor().getId().equals(descriptor.getId()));
		if(log.isDebugEnabled()){
			log.debug("Retrieving attributes via resolver " +
					"id=\"{}\" name=\"{}\"", 
					descriptor.getId(), descriptor.getName());
		}
		TimerContext timerCtx = attrResolveTimer.time();
		try
		{
			return AttributeSet
					.builder(descriptor)
					.attributes(doResolve(context))
					.ticker(context.getTicker())
					.build();
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			failuresCount.inc();
			throw e;
		}finally{
			timerCtx.stop();
		}
	}
	
	/**
	 * Performs actual attribute resolution
	 * 
	 * @param context a policy information context
	 * @return a resolved map of attribute values as instances a
	 *  {@link BagOfAttributeExp} mapped by an attribute identifier
	 * @throws Exception if an error occurs
	 */
	protected abstract Map<String, BagOfAttributeExp> doResolve(
			ResolverContext context) throws Exception;

	@Override
	public final int getPreferredCacheTTL() {
		return descriptor.getPreferreredCacheTTL();
	}
	
	public void reset(){
		attrResolveTimer.clear();
		failuresCount.clear();
	}
	
	@Override
	public final void setPreferredCacheTTL(int ttl) {
		if(descriptor.isCachable() 
				&& ttl > 0){
			this.preferedCacheTTL.set(ttl);
		}
	}
}
