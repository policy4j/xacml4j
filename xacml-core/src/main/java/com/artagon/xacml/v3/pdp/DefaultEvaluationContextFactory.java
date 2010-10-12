package com.artagon.xacml.v3.pdp;

import com.artagon.xacml.v3.BaseEvaluationContext;
import com.artagon.xacml.v3.DefaultRequestContextCallback;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.EvaluationContextHandler;
import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;
import com.google.common.base.Preconditions;

public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
	private PolicyReferenceResolver policyReferenceResolver;
	private XPathProvider xpathProvider;
	private XPathVersion defaultXPathVersion = XPathVersion.XPATH1;
	private boolean validateFuncParamsAtRuntime = false;
	private PolicyInformationPoint pip;
	
	/**
	 * Constructs default evaluation context factory
	 * 
	 * @param referenceResolver a policy reference resolver
	 * @param pip a policy information point
	 */
	public DefaultEvaluationContextFactory(
			PolicyRepository repository, 
			PolicyInformationPoint pip){
		this(repository, new DefaultXPathProvider(), pip);
	}
	
	/**
	 * Constructs default evaluation context factory
	 * 
	 * @param referenceResolver a policy reference resolver
	 * @param xpathProvider an XPath provider
	 * @param pip a policy information point
	 */
	public DefaultEvaluationContextFactory(
			PolicyRepository repository, 
			XPathProvider xpathProvider, 
			PolicyInformationPoint pip){
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(xpathProvider);
		Preconditions.checkNotNull(pip);
		this.policyReferenceResolver = new DefaultPolicyReferenceResolver(repository);
		this.xpathProvider = xpathProvider;
		this.pip = pip;
	}
	
	@Override
	public XPathProvider getXPathProvider() {
		return xpathProvider;
	}

	@Override
	public EvaluationContext createContext(RequestContext request) 
	{
		RequestContextCallback callback = new DefaultRequestContextCallback(request);
		EvaluationContextHandler handler = new DefaultEvaluationContextHandler(
				callback, xpathProvider, pip);
		return new RootEvaluationContext(handler);
	}
	
	class RootEvaluationContext extends BaseEvaluationContext
	{
		public RootEvaluationContext(EvaluationContextHandler contextHandler) {
			super(
					DefaultEvaluationContextFactory.this.validateFuncParamsAtRuntime,
					contextHandler,
					DefaultEvaluationContextFactory.this.policyReferenceResolver);
		}

		@Override
		public XPathVersion getXPathVersion() { 
			return DefaultEvaluationContextFactory.this.defaultXPathVersion;
		}
		
	}
}
