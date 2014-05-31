package org.xacml4j.v30.spi.audit;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;


public class BasePolicyDecisionAuditor
	implements PolicyDecisionAuditor
{
	private final static Logger log = LoggerFactory.getLogger(BasePolicyDecisionAuditor.class);

	@Override
	public final void audit(PolicyDecisionPoint pdp, Result result, RequestContext req)
	{
		if(log.isDebugEnabled()){
			log.debug("Auditing access decision=\"{}\" " +
					"for request=\"{}\"", result, req);
		}
		if(isAuditable(pdp, req)){
			doAudit(pdp, req, result);
		}
	}

	/**
	 * Invokes appropriate audit hook based
	 * on decision result
	 *
	 * @param req a decision request
	 * @param result a decision result
	 */
	private void doAudit(PolicyDecisionPoint pdp, RequestContext req, Result result)
	{
		switch(result.getDecision()){
			case DENY :
					doDenyAudit(pdp, req, result);
					break;
			case PERMIT:
					doPermitAudit(pdp, req, result);
					break;
			case NOT_APPLICABLE:
					doNotApplicableAudit(pdp, req, result);
					break;
			case INDETERMINATE:
			case INDETERMINATE_D:
			case INDETERMINATE_P:
			case INDETERMINATE_DP:
					doIndeterminateAudit(pdp, req, result);
					break;
		}
	}

	/**
	 * Tests if a given {@link RequestContext} need to be audited
	 *
	 * @param req a decision request
	 * @return {@code true} if request needs to be audited
	 */
	protected boolean isAuditable(PolicyDecisionPoint pdp, RequestContext req){
		return false;
	}

	/**
	 * Invokes in case {@link Result#getDecision()} returns
	 * {@link Decision#PERMIT}
	 *
	 * @param req a decision request
	 * @param result a decision result
	 */
	protected void doPermitAudit(PolicyDecisionPoint pdp, RequestContext req, Result result){

	}

	/**
	 * Invokes in case {@link Result#getDecision()} returns
	 * {@link Decision#DENY}
	 *
	 * @param req a decision request
	 * @param result a decision result
	 */
	protected void doDenyAudit(PolicyDecisionPoint pdp, RequestContext req, Result result){

	}

	/**
	 * Invokes in case {@link Result#getDecision()} returns
	 * {@link Decision#DENY}
	 *
	 * @param req a decision request
	 * @param result a decision result
	 */
	protected void doNotApplicableAudit(PolicyDecisionPoint pdp, RequestContext req, Result result){

	}

	protected void doIndeterminateAudit(PolicyDecisionPoint pdp, RequestContext req, Result result){

	}
}
