package org.xacml4j.v30.pdp;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.DefaultPolicyReferenceResolver;
import org.xacml4j.v30.spi.repository.PolicyReferenceResolver;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.spi.xpath.XPathProvider;
import org.xacml4j.v30.types.Types;

import com.google.common.base.Preconditions;

final class DefaultPolicyDecisionPointContextFactory
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
	private int decisionCacheTTL = 30;
	private XPathVersion defaultXPathVersion = XPathVersion.XPATH1;
	private Types types;

	DefaultPolicyDecisionPointContextFactory(
			Types types,
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
		Preconditions.checkArgument(types != null);
		this.types = types;
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
						types,
						validateFuncParamsAtRuntime,
						decisionCacheTTL,
						defaultXPathVersion,
						policyReferenceResolver,
						handler);
			}
		};
	}
}
