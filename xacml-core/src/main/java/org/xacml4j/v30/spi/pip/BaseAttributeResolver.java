package org.xacml4j.v30.spi.pip;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.BagOfAttributeExp;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static org.xacml4j.v30.pdp.MetricsSupport.name;
import static org.xacml4j.v30.pdp.MetricsSupport.getOrCreate;
import static com.google.common.base.Preconditions.*;

/**
 * A base implementation of {@link AttributeResolver}
 *
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeResolver implements AttributeResolver
{
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private AttributeResolverDescriptorDelegate descriptor;

	private Timer timer;
	private Histogram histogram;
	private AtomicInteger preferedCacheTTL;
	private MetricRegistry registry;

	protected BaseAttributeResolver(
			AttributeResolverDescriptor descriptor){
		checkNotNull(descriptor);
		this.descriptor = new AttributeResolverDescriptorDelegate(descriptor){
			@Override
			public int getPreferreredCacheTTL() {
				return (preferedCacheTTL == null)?
						super.getPreferreredCacheTTL():preferedCacheTTL.get();
			}
		};
		this.registry = getOrCreate();
		this.timer = registry.timer(name("pip.AttributeResolver", 
				descriptor.getId(), "timer"));
		this.histogram = registry.histogram(name("pip.AttributeResolver", 
				descriptor.getId(), "histogram"));
	}

	@Override
	public final AttributeResolverDescriptor getDescriptor(){
		return descriptor;
	}

	@Override
	public final AttributeSet resolve(
			ResolverContext context) throws Exception
	{
		checkArgument(context.getDescriptor().getId().equals(descriptor.getId()));
		if(log.isDebugEnabled()){
			log.debug("Retrieving attributes via resolver " +
					"id=\"{}\" name=\"{}\"",
					descriptor.getId(), descriptor.getName());
		}
		Timer.Context timerCtx = timer.time();
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
			throw e;
		}finally{
			histogram.update(timerCtx.stop());
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

	@Override
	public final void setPreferredCacheTTL(int ttl) {
		if(descriptor.isCachable()
				&& ttl > 0){
			this.preferedCacheTTL.set(ttl);
		}
	}
}
