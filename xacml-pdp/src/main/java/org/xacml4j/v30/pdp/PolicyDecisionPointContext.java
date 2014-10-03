package org.xacml4j.v30.pdp;

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

import org.xacml4j.v30.*;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.xpath.XPathProvider;

public interface PolicyDecisionPointContext
{
	/**
	 * Gets correlation identifier used
	 * to track request in log messages
	 *
	 * @return correlation identifier
	 */
	String getCorrelationId();

	/**
	 * Creates {@link EvaluationContext} to evaluate
	 * given {@link RequestContext} access decision request
	 *
	 * @param req an access decision request
	 * @return {@link EvaluationContext}
	 */
	RootEvaluationContext createEvaluationContext(RequestContext req);

	/**
	 * Gets root policy for authorization domain
	 *
	 * @return {@link CompositeDecisionRule} a root
	 * policy
	 */
	DecisionRule getDomainPolicy();

	XPathProvider getXPathProvider();

	PolicyDecisionCache getDecisionCache();

	PolicyDecisionAuditor getDecisionAuditor();

	Result requestDecision(RequestContext req);

	RequestContextHandler getRequestHandlers();

	boolean isDecisionCacheEnabled();
	boolean isDecisionAuditEnabled();
	boolean isValidateFuncParamsAtRuntime();
}
