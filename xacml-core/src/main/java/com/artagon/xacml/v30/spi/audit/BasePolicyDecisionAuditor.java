package com.artagon.xacml.v30.spi.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;

public class BasePolicyDecisionAuditor implements PolicyDecisionAuditor 
{
	private final static Logger log = LoggerFactory.getLogger(BasePolicyDecisionAuditor.class);
	
	@Override
	public final void audit(Result result, RequestContext req) {
		if(isAuditable(req)){
			doAudit(req, result);
		}
	}
	
	/**
	 * Invokes appropriate audit hook based
	 * on decision result
	 * 
	 * @param req a decision request
	 * @param result a decision result
	 */
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
	
	/**
	 * Invokes in case {@link Result#getDecision()} returns
	 * {@link Decision#PERMIT}
	 * 
	 * @param req a decision request
	 * @param result a decision result
	 */
	protected void doPermitAudit(RequestContext req, Result result){
		
	}
	
	/**
	 * Invokes in case {@link Result#getDecision()} returns
	 * {@link Decision#DENY}
	 * 
	 * @param req a decision request
	 * @param result a decision result
	 */
	protected void doDenyAudit(RequestContext req, Result result){
		
	}
	
	/**
	 * Invokes in case {@link Result#getDecision()} returns
	 * {@link Decision#DENY}
	 * 
	 * @param req a decision request
	 * @param result a decision result
	 */
	protected void doNotApplicableAudit(RequestContext req, Result result){
		
	}
	
	protected void doIndeterminateAudit(RequestContext req, Result result){
		
	}
}
