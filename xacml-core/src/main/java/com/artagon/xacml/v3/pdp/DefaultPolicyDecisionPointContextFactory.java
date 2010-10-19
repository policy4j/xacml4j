package com.artagon.xacml.v3.pdp;

import java.util.List;

import com.artagon.xacml.v3.DefaultRequestContextCallback;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextHandler;
import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.RootEvaluationContext;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.google.common.base.Preconditions;

public class DefaultPolicyDecisionPointContextFactory 
	implements PolicyDecisionPointContextFactory
{
	private PolicyInformationPoint pip;
	private PolicyDecisionAuditor decisionAuditor;
	private PolicyDecisionCache decisionCache;
	private XPathProvider xpathProvider;
	private PolicyRepository policyRepository;
	private PolicyReferenceResolver policyReferenceResolver;
	private PolicyDomain policyDomain;
	private RequestContextHandlerChain requestHandlers;
	
	private boolean validateFuncParamsAtRuntime = false;
	private XPathVersion defaultXPathVersion = XPathVersion.XPATH1;
	
	public DefaultPolicyDecisionPointContextFactory(
			PolicyDomain policyDomain, 
			PolicyRepository repository, 
			XPathProvider xpathProvider, 
			PolicyInformationPoint pip,
			List<RequestContextHandler> handlers)
	{
		Preconditions.checkArgument(policyDomain != null);
		Preconditions.checkArgument(repository != null);
		Preconditions.checkArgument(xpathProvider != null);
		Preconditions.checkArgument(pip != null);
		this.policyReferenceResolver = new DefaultPolicyReferenceResolver(repository);
		this.pip = pip;
		this.xpathProvider = xpathProvider;
		this.policyDomain = policyDomain;
		this.policyRepository = repository;
		this.requestHandlers = new RequestContextHandlerChain(handlers);
	}

	@Override
	public PolicyDecisionPointContext createContext(final PolicyDecisionCallback pdp) 
	{
		return new PolicyDecisionPointContext() {
			
			@Override
			public XPathProvider getXPathProvider() {
				return xpathProvider;
			}
			
			@Override
			public PolicyDomain getPolicyDomain() {
				return policyDomain;
			}
			
			@Override
			public PolicyDecisionCache getDecisionCache() {
				return decisionCache;
			}
			
			@Override
			public PolicyDecisionAuditor getAuditor() {
				return decisionAuditor;
			}
			
			public PolicyDecisionCallback getPolicyDecisionCallback(){
				return pdp;
			}
			
			@Override
			public RequestContextHandlerChain getRequestHandlers() {
				return requestHandlers;
			}

			@Override
			public EvaluationContext createEvaluationContext(RequestContext request) {
				RequestContextCallback callback = new DefaultRequestContextCallback(request);
				EvaluationContextHandler handler = new DefaultEvaluationContextHandler(
						callback, xpathProvider, pip);
				return new RootEvaluationContext(validateFuncParamsAtRuntime, 
						defaultXPathVersion, policyReferenceResolver, handler);
			}
		};
	}
	

}
