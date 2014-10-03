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

import java.util.Random;

import org.xacml4j.v30.*;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.DefaultPolicyReferenceResolver;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.xpath.XPathProvider;

import com.google.common.base.Preconditions;

final class DefaultPolicyDecisionPointContextFactory
	implements PolicyDecisionPointContextFactory
{
	private final static Random RND = new Random();

	private PolicyInformationPoint pip;
	private PolicyDecisionAuditor decisionAuditor;
	private PolicyDecisionCache decisionCache;
	private XPathProvider xpathProvider;
	private PolicyReferenceResolver policyReferenceResolver;
	private DecisionRule policyDomain;
	private RequestContextHandlerChain requestHandlers;

	private boolean decisionCacheEnabled = true;
	private boolean decisionAuditEnabled = true;
	private boolean validateFuncParamsAtRuntime = false;
	private int decisionCacheTTL = 30;
	private XPathVersion defaultXPathVersion = XPathVersion.XPATH1;

	DefaultPolicyDecisionPointContextFactory(
			DecisionRule policyDomain,
			PolicyRepository repository,
			PolicyDecisionAuditor auditor,
			PolicyDecisionCache cache,
			XPathProvider xpathProvider,
			PolicyInformationPoint pip,
			RequestContextHandlerChain handlerChain)
	{
		Preconditions.checkArgument(policyDomain != null);
		Preconditions.checkArgument(repository != null);
		Preconditions.checkArgument(xpathProvider != null);
		Preconditions.checkArgument(pip != null);
		Preconditions.checkArgument(auditor != null);
		Preconditions.checkArgument(cache != null);
		this.policyReferenceResolver = new DefaultPolicyReferenceResolver(repository);
		this.pip = pip;
		this.xpathProvider = xpathProvider;
		this.policyDomain = policyDomain;
		this.decisionAuditor = auditor;
		this.decisionCache = cache;
		this.requestHandlers = handlerChain;
	}

	@Override
	public int getDefaultDecisionCacheTTL(){
		return decisionCacheTTL;
	}

	public void setDefaultDecisionCacheTTL(int ttl){
		this.decisionCacheTTL = (ttl > 0)?ttl:0;
	}

	public void setValidateFunctionParametersAtRuntime(
			boolean validate){
		this.validateFuncParamsAtRuntime = validate;
	}

	@Override
	public boolean isDecisionAuditEnabled() {
		return decisionAuditEnabled;
	}

	@Override
	public boolean isDecisionCacheEnabled() {
		return decisionCacheEnabled;
	}

	@Override
	public PolicyDecisionPointContext createContext(final PolicyDecisionCallback pdp)
	{
		final String correlationId = Long.toHexString(RND.nextLong());
		return new PolicyDecisionPointContext() {

			@Override
			public boolean isDecisionCacheEnabled(){
				return decisionCacheEnabled;
			}

			@Override
			public boolean isValidateFuncParamsAtRuntime(){
				return validateFuncParamsAtRuntime;
			}

			@Override
			public boolean isDecisionAuditEnabled(){
				return decisionAuditEnabled;
			}

			@Override
			public XPathProvider getXPathProvider() {
				return xpathProvider;
			}

			@Override
			public DecisionRule getDomainPolicy() {
				return policyDomain;
			}

			@Override
			public PolicyDecisionCache getDecisionCache() {
				return decisionCache;
			}

			@Override
			public PolicyDecisionAuditor getDecisionAuditor() {
				return decisionAuditor;
			}

			@Override
			public Result requestDecision(RequestContext req) {

				return pdp.requestDecision(this, req);
			}

			@Override
			public RequestContextHandlerChain getRequestHandlers() {
				return requestHandlers;
			}

			@Override
			public String getCorrelationId() {
				return correlationId;
			}

			@Override
			public RootEvaluationContext createEvaluationContext(RequestContext request)
			{
				Preconditions.checkArgument(!request.containsRepeatingCategories());
				Preconditions.checkArgument(!request.containsRequestReferences());
				RequestContextCallback callback = new DefaultRequestContextCallback(request);
				EvaluationContextHandler handler = new DefaultEvaluationContextHandler(
						callback, xpathProvider, pip);
				return new RootEvaluationContext(
						validateFuncParamsAtRuntime,
						decisionCacheTTL,
						defaultXPathVersion,
						policyReferenceResolver,
						handler);
			}
		};
	}
}
