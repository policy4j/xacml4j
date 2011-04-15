package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationContextHandler;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.RootEvaluationContext;
import com.artagon.xacml.v30.XPathVersion;
import com.artagon.xacml.v30.spi.audit.PolicyDecisionAuditor;
import com.artagon.xacml.v30.spi.pdp.PolicyDecisionCache;
import com.artagon.xacml.v30.spi.pdp.RequestContextHandlerChain;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.repository.DefaultPolicyReferenceResolver;
import com.artagon.xacml.v30.spi.repository.PolicyReferenceResolver;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;
import com.google.common.base.Preconditions;

public class DefaultPolicyDecisionPointContextFactory 
	implements PolicyDecisionPointContextFactory
{	
	private PolicyInformationPoint pip;
	private PolicyDecisionAuditor decisionAuditor;
	private PolicyDecisionCache decisionCache;
	private XPathProvider xpathProvider;
	private PolicyReferenceResolver policyReferenceResolver;
	private CompositeDecisionRule policyDomain;
	private RequestContextHandlerChain requestHandlers;
	
	private boolean decisionCacheEnabled = true;
	private boolean decisionAuditEnabled = true;
	private boolean validateFuncParamsAtRuntime = false;
	private XPathVersion defaultXPathVersion = XPathVersion.XPATH1;
	
	public DefaultPolicyDecisionPointContextFactory(
			CompositeDecisionRule policyDomain, 
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
	
	public void setValidaFunctionParametersAtRuntime(
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
			public CompositeDecisionRule getDomainPolicy() {
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
			public EvaluationContext createEvaluationContext(RequestContext request) 
			{
				Preconditions.checkArgument(!request.containsRepeatingCategories());
				Preconditions.checkArgument(!request.containsRequestReferences());
				RequestContextCallback callback = new DefaultRequestContextCallback(request);
				EvaluationContextHandler handler = new DefaultEvaluationContextHandler(
						callback, xpathProvider, pip);
				return new RootEvaluationContext(
						validateFuncParamsAtRuntime, 
						defaultXPathVersion, 
						policyReferenceResolver, 
						handler);
			}
		};
	}
}
