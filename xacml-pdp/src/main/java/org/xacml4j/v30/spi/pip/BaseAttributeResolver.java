package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.BagOfAttributeExp;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.xacml4j.v30.pdp.MetricsSupport.getOrCreate;
import static org.xacml4j.v30.pdp.MetricsSupport.name;

/**
 * A base implementation of {@link AttributeResolver}
 *
 * @author Giedrius Trumpickas
 */
public abstract class BaseAttributeResolver implements AttributeResolver
{
	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final AttributeResolverDescriptorDelegate descriptor;

	private final Timer timer;
	private final Histogram histogram;
	private AtomicInteger preferredCacheTTL;

	protected BaseAttributeResolver(
			AttributeResolverDescriptor descriptor){
		checkNotNull(descriptor);
		this.descriptor = new AttributeResolverDescriptorDelegate(descriptor){
			@Override
			public int getPreferredCacheTTL() {
				return (preferredCacheTTL == null)?
						super.getPreferredCacheTTL(): preferredCacheTTL.get();
			}
		};
		final MetricRegistry registry = getOrCreate();
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
			log.debug("Retrieving category via resolver attributeId=\"{}\" name=\"{}\"",
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
	 * Performs actual category resolution
	 *
	 * @param context a policy information context
	 * @return a resolved map of category values as instances a
	 *  {@link BagOfAttributeExp} mapped by an category identifier
	 * @throws Exception if an error occurs
	 */
	protected abstract Map<String, BagOfAttributeExp> doResolve(
			ResolverContext context) throws Exception;

	@Override
	public final int getPreferredCacheTTL() {
		return descriptor.getPreferredCacheTTL();
	}

	@Override
	public final void setPreferredCacheTTL(int ttl) {
		if(descriptor.isCacheable()
				&& ttl > 0){
			this.preferredCacheTTL.set(ttl);
		}
	}
}
