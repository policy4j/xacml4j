package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;

public class BaseDecisionCache implements PolicyDecisionCache
{
	@Override
	public Result getDecision(RequestContext req){
		return isCachable(req)?doGetDecision(req):null;
	}

	@Override
	public void putDecision(RequestContext req, Result result) {
		if(isCachable(req)){
			doPutDecision(req, result);
		}
	}
	
	protected boolean isCachable(RequestContext context){
		return false;
	}
	
	protected void doPutDecision(RequestContext req, Result res){
		
	}
	
	protected Result doGetDecision(RequestContext request){
		return null;
	}
}
