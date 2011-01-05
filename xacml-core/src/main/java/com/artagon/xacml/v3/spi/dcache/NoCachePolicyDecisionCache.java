package com.artagon.xacml.v3.spi.dcache;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

public class NoCachePolicyDecisionCache implements PolicyDecisionCache
{
	@Override
	public Result getDecision(RequestContext req) {
		return null;
	}

	@Override
	public void putDecision(RequestContext req, Result res) {
	}
}
