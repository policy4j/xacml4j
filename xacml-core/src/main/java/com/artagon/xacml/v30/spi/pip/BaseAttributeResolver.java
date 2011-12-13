package com.artagon.xacml.v30.spi.pip;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.google.common.base.Preconditions;

/**
 * A base implementation of {@link AttributeResolver}
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeResolver implements AttributeResolver
{	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private AttributeResolverDescriptorDelegate descriptor;
	private AtomicLong failuresCount;
	private AtomicLong successCount;
	private AtomicInteger preferedCacheTTL;
	private AtomicLong successInvocationTimeCMA;
	
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
		this.failuresCount = new AtomicLong(0);
		this.successCount = new AtomicLong(0);
		this.successInvocationTimeCMA = new AtomicLong(0);
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
		try
		{
			long start = System.currentTimeMillis();
			Map<String, BagOfAttributeExp> v = doResolve(context);
			long n = successCount.incrementAndGet();
			long time = (System.currentTimeMillis() - start);
			successInvocationTimeCMA.set((time  + (n - 1) * successCount.incrementAndGet()) / n);
			if(log.isDebugEnabled()){
				log.debug("Attribute resolver id=\"{}\" " +
						"invocation took=\"{}\" miliseconds", getId(), time);
			}
			return new AttributeSet(descriptor, 
					(v != null)?v:Collections.<String, BagOfAttributeExp>emptyMap());
		}catch(Exception e){
			failuresCount.incrementAndGet();
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw e;
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
	public final String getId() {
		return descriptor.getId();
	}

	@Override
	public final long getInvocationCount() {
		return successCount.get() + 
				failuresCount.get();
	}

	@Override
	public long getSuccessCount() {
		return successCount.get();
	}

	@Override
	public final long getFailuresCount() {
		return failuresCount.get();
	}

	@Override
	public final long getSuccessInvocationTimeCMA() {
		return successInvocationTimeCMA.get();
	}

	@Override
	public final int getPreferredCacheTTL() {
		return descriptor.getPreferreredCacheTTL();
	}
	
	public void reset(){
		successCount.set(0);
		failuresCount.set(0);
		successInvocationTimeCMA.set(0);
	}
	@Override
	public final void setPreferredCacheTTL(int ttl) {
		if(descriptor.isCachable() 
				&& ttl > 0){
			this.preferedCacheTTL.set(ttl);
		}
	}
}
