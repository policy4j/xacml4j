package com.artagon.xacml.v3;

import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.spi.xpath.DefaultXPathProvider;
import com.google.common.base.Preconditions;

public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
	private PolicyRepository repository;
	private XPathProvider xpathProvider;
	private XPathVersion defaultXPathVersion = XPathVersion.XPATH1;
	private boolean validateFuncParamsAtRuntime = false;
	
	public DefaultEvaluationContextFactory(PolicyRepository repository){
		this(repository, new DefaultXPathProvider());
	}
	
	public DefaultEvaluationContextFactory(
			PolicyRepository repository, 
			XPathProvider xpathProvider){
		Preconditions.checkNotNull(repository);
		Preconditions.checkNotNull(xpathProvider);
		this.repository = repository;
		this.xpathProvider = xpathProvider;
	}

	@Override
	public EvaluationContext createContext(Request request) 
	{
		ContextHandler handler = new DefaultContextHandler(xpathProvider, request);
		return new RootEvaluationContext(handler);
	}
	
	class RootEvaluationContext extends BaseEvaluationContext
	{
		public RootEvaluationContext(ContextHandler contextHandler) {
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
