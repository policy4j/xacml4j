package com.artagon.xacml.v3;

import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;
import com.google.common.base.Preconditions;

public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
	private PolicyDomain repository;
	private XPathProvider xpathProvider;
	private XPathVersion defaultXPathVersion = XPathVersion.XPATH1;
	private boolean validateFuncParamsAtRuntime = false;
	private PolicyInformationPoint pip;
	
	public DefaultEvaluationContextFactory(
			PolicyDomain repository, 
			PolicyInformationPoint pip){
		this(repository, new DefaultXPathProvider(), pip);
	}
	
	public DefaultEvaluationContextFactory(
			PolicyDomain repository, 
			XPathProvider xpathProvider, 
			PolicyInformationPoint pip){
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(xpathProvider);
		Preconditions.checkNotNull(pip);
		this.repository = repository;
		this.xpathProvider = xpathProvider;
		this.pip = pip;
	}

	@Override
	public EvaluationContext createContext(RequestContext request) 
	{
		EvaluationContextHandler handler = new DefaultContextHandler(xpathProvider, request, pip);
		return new RootEvaluationContext(handler);
	}
	
	class RootEvaluationContext extends BaseEvaluationContext
	{
		public RootEvaluationContext(EvaluationContextHandler contextHandler) {
			super(
					DefaultEvaluationContextFactory.this.validateFuncParamsAtRuntime,
					contextHandler,
					DefaultEvaluationContextFactory.this.xpathProvider, 
					DefaultEvaluationContextFactory.this.repository);
		}

		@Override
		public XPathVersion getXPathVersion() { 
			return DefaultEvaluationContextFactory.this.defaultXPathVersion;
		}
		
	}
}
