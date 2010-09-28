package com.artagon.xacml.v3.pdp.audit;

import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.pdp.PolicyDecisionAuditor;

public class BasePolicyDecisionAuditor implements PolicyDecisionAuditor 
{
	@Override
	public final void audit(Result result, RequestContext req) {
		if(isAuditable(req)){
			doAudit(req, result);
		}
	}
	
	private void doAudit(RequestContext req, Result result)
	{
		switch(result.getDecision()){
			case DENY :
					doDenyAudit(req, result);
					break;
			case PERMIT:
					doPermitAudit(req, result);
					break;
			case NOT_APPLICABLE:
					doNotApplicableAudit(req, result);
					break;
			case INDETERMINATE:
			case INDETERMINATE_D:
			case INDETERMINATE_P:
			case INDETERMINATE_DP:
					doIndeterminateAudit(req, result);
					break;
		}
	}
	
	/**
	 * Tests if a given {@link RequestContext} need to be audited
	 * 
	 * @param req a decision request
	 * @return <code>true</code> if request needs to be audited
	 */
	protected boolean isAuditable(RequestContext req){
		return false;
	}
	
	protected void doPermitAudit(RequestContext req, Result result){
		
	}
	
	protected void doDenyAudit(RequestContext req, Result result){
		
	}
	
	protected void doNotApplicableAudit(RequestContext req, Result result){
		
	}
	
	protected void doIndeterminateAudit(RequestContext req, Result result){
		
	}
}
