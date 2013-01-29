package org.xacml4j.v30.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class AbstractRequestContextHandler 
	implements RequestContextHandler
{
	private AtomicReference<RequestContextHandler> next;
	private List<String> features;
	
	protected AbstractRequestContextHandler(String featureId)
	{
		Preconditions.checkNotNull(featureId);
		this.features = ImmutableList.of(featureId);
		this.next = new AtomicReference<RequestContextHandler>();
	}
	
	@Override
	public Collection<String> getFeatures(){
		return features;
	}
	
	/**
	 * A helper method to be used by implementations
	 * to invoke a next handler in the chain
	 * 
	 * @param request a decision request
	 * @param pdp a policy decision point callback
	 * @return collection of  {@link Result} instances
	 */
	protected final Collection<Result> handleNext(
			RequestContext request, 
			PolicyDecisionPointContext context)
	{
		RequestContextHandler h = next.get();
		return (h == null)?
				Collections.singleton(context.requestDecision(request)):
					h.handle(request, context);
	}

	/**
	 * @exception IllegalStateException if this handler
	 * already has handler set
	 */
	@Override
	public final void setNext(RequestContextHandler handler) {
		Preconditions.checkNotNull(handler);
		Preconditions.checkState(next.get() == null, 
				"Handler is already has next handler");
		this.next.set(handler);
	}
}
