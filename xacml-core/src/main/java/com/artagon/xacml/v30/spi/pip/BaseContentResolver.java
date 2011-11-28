package com.artagon.xacml.v30.spi.pip;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.base.Preconditions;

/**
 * A base class for {@link ContentResolver} implementations
 * 
 * @author Giedrius Trumpickas
 */
public abstract class BaseContentResolver implements ContentResolver
{
	private final static Logger log = LoggerFactory.getLogger(BaseContentResolver.class);
	
	private ContentResolverDescriptor descriptor;
	private AtomicLong invocationCount;
	private AtomicLong failuresCount;
	private AtomicLong successCount;
	private AtomicLong lastInvocationDuration;
	private AtomicInteger preferedCacheTTL;

	protected BaseContentResolver(ContentResolverDescriptor descriptor){
		Preconditions.checkArgument(descriptor != null);
		this.descriptor = new ContentResolverDescriptorDelegate(descriptor){
			@Override
			public int getPreferreredCacheTTL() {
				return (preferedCacheTTL == null)?
						super.getPreferreredCacheTTL():preferedCacheTTL.get();
			}
		};
		this.invocationCount = new AtomicLong();
		this.failuresCount = new AtomicLong();
		this.successCount = new AtomicLong();
		this.lastInvocationDuration = new AtomicLong();
	}
	
	@Override
	public final ContentResolverDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public final Content resolve(
			ResolverContext context) throws Exception 
	{
		Preconditions.checkArgument(context.getDescriptor() == descriptor);
		if(log.isDebugEnabled()){
			log.debug("Retrieving content via resolver " +
					"id=\"{}\" name=\"{}\"", 
					descriptor.getId(), 
					descriptor.getName());
		}
		try{
			invocationCount.incrementAndGet();
			long start = System.currentTimeMillis();
			Node node = doResolve(context);
			lastInvocationDuration.set(System.currentTimeMillis() - start);
			successCount.incrementAndGet();
			return new Content(descriptor, node);
		}catch(Exception e){
			failuresCount.incrementAndGet();
			throw e;
		}		
	}
	
	/**
	 * Performs an actual content resolution
	 * 
	 * @param context a policy information context
	 * @return {@link Node} a resolved content or <code>null</code>
	 * @throws Exception if an error occurs
	 */
	abstract Node doResolve(
			ResolverContext context) 
		throws Exception;

	@Override
	public String getId() {
		return descriptor.getId();
	}

	@Override
	public long getInvocationCount() {
		return invocationCount.get();
	}

	@Override
	public long getFailuresCount() {
		return failuresCount.get();
	}

	@Override
	public long getSuccessCount() {
		return successCount.get();
	}

	@Override
	public long getLastInvocationTime() {
		return lastInvocationDuration.get();
	}
	
	@Override
	public final int getPreferredCacheTTL() {
		return descriptor.getPreferreredCacheTTL();
	}

	@Override
	public final void setPreferredCacheTTL(int ttl) {
		if(descriptor.isCachable() 
				&& ttl > 0){
			this.preferedCacheTTL.set(ttl);
		}
	}
}
