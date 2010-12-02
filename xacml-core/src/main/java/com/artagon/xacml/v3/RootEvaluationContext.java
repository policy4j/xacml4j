package com.artagon.xacml.v3;

import com.artagon.xacml.v3.spi.PolicyRepository;

public final class RootEvaluationContext extends BaseEvaluationContext
{
	private XPathVersion defaultXPathVersion;
	
	public RootEvaluationContext(boolean validateFuncParamsAtRuntime, 
			XPathVersion defaultXPathVersion, 
			PolicyRepository referenceResolver,
			EvaluationContextHandler contextHandler) {
		super(validateFuncParamsAtRuntime,
				contextHandler,
				referenceResolver);
		this.defaultXPathVersion = defaultXPathVersion;
	}
	
	public RootEvaluationContext(boolean validateFuncParamsAtRuntime, 
			PolicyRepository referenceResolver, EvaluationContextHandler handler){
		this(validateFuncParamsAtRuntime, 
				XPathVersion.XPATH1, referenceResolver, handler);
	}

	@Override
	public XPathVersion getXPathVersion() { 
		return defaultXPathVersion;
	}	
}


