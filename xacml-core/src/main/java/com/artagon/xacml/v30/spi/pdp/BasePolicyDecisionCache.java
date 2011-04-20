package com.artagon.xacml.v30.spi.pdp;

import java.util.concurrent.atomic.AtomicLong;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;


public abstract class BasePolicyDecisionCache 
	extends StandardMBean implements PolicyDecisionCache, PolicyDecisionCacheMBean
{
	private AtomicLong cacheMiss = new AtomicLong(0);
	private AtomicLong cacheHit = new AtomicLong(0); 
	
	protected BasePolicyDecisionCache() throws NotCompliantMBeanException{
		super(PolicyDecisionCacheMBean.class);
	}
	
	@Override
	public final Result getDecision(RequestContext req) {
		
		Result d =  doGetDecision(req);
		if(d == null){
			cacheMiss.incrementAndGet();
			return d;
		}
		cacheHit.incrementAndGet();
		return d;
	}

	@Override
	public final void putDecision(RequestContext req, Result result, int ttl) {
		
	}

	protected abstract Result doGetDecision(RequestContext req);
	protected abstract void doPutDecision(RequestContext req, Result result);
	
	@Override
	public final long getCacheHitCount() {
		return cacheHit.get();
	}

	@Override
	public long getCacheMissCount() {
		return cacheMiss.get();
	}

	@Override
	public void resetCount() {
		this.cacheHit.set(0);
		this.cacheMiss.set(0);
	}
}
