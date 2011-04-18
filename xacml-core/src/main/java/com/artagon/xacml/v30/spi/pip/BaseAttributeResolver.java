package com.artagon.xacml.v30.spi.pip;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.BagOfAttributeValues;
import com.google.common.base.Preconditions;

/**
 * A base implementation of {@link AttributeResolver}
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeResolver implements AttributeResolver
{	
	private final static Logger log = LoggerFactory.getLogger(BaseAttributeResolver.class);
	
	private AttributeResolverDescriptorDelegate descriptor;
	private AtomicLong invocationCount;
	private AtomicLong failuresCount;
	private AtomicLong successCount;
	private AtomicLong lastInvocationDuration;
	private AtomicInteger preferedCacheTTL;
	
	protected BaseAttributeResolver(AttributeResolverDescriptor descriptor){
		Preconditions.checkNotNull(descriptor);
		this.descriptor = new AttributeResolverDescriptorDelegate(descriptor){
			@Override
			public int getPreferreredCacheTTL() {
				return (preferedCacheTTL == null)?
						super.getPreferreredCacheTTL():preferedCacheTTL.get();
			}
		};
		this.invocationCount = new AtomicLong(0);
		this.failuresCount = new AtomicLong(0);
		this.successCount = new AtomicLong(0);
		this.lastInvocationDuration = new AtomicLong(0);
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
			invocationCount.incrementAndGet();
			long start = System.currentTimeMillis();
			Map<String, BagOfAttributeValues> v = doResolve(context);
			lastInvocationDuration.set(System.currentTimeMillis() - start);
			successCount.incrementAndGet();
			return new AttributeSet(descriptor, 
					(v != null)?v:Collections.<String, BagOfAttributeValues>emptyMap());
		}catch(Exception e){
			failuresCount.incrementAndGet();
			throw e;
		}
	}
	
	/**
	 * Performs actual attribute resolution
	 * 
	 * @param context a policy information context
	 * @return a resolved map of attribute values as instances a
	 *  {@link BagOfAttributeValues} mapped by an attribute identifier
	 * @throws Exception if an error occurs
	 */
	abstract Map<String, BagOfAttributeValues> doResolve(
			ResolverContext context) throws Exception;

	@Override
	public final String getId() {
		return descriptor.getId();
	}

	@Override
	public final long getInvocationCount() {
		return invocationCount.get();
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
	public final long getLastInvocationDuration() {
		return lastInvocationDuration.get();
	}

	@Override
	public final int getPreferredCacheTTL() {
		return descriptor.getPreferreredCacheTTL();
	}

	@Override
	public final void setPreferredCacheTTL(int ttl) {
		this.preferedCacheTTL.set(ttl);
	}
}
