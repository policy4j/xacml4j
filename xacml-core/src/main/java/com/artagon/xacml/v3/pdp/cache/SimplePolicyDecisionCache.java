package com.artagon.xacml.v3.pdp.cache;

import java.util.HashMap;
import java.util.Map;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionCache;

public class SimplePolicyDecisionCache implements PolicyDecisionCache
{
	private Map<RequestContext, Result> cache;
	
	public SimplePolicyDecisionCache(){
		this.cache = new HashMap<RequestContext, Result>();
	}
	
	@Override
	public Result getDecision(RequestContext req) {
		return cache.get(req);
	}

	@Override
	public void putDecision(RequestContext req, Result result) {
		cache.put(req, result);
	}
}
