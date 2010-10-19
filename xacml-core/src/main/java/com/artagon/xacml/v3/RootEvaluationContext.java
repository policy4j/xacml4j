package com.artagon.xacml.v3;

public final class RootEvaluationContext extends BaseEvaluationContext
{
	private XPathVersion defaultXPathVersion;
	
	public RootEvaluationContext(boolean validateFuncParamsAtRuntime, 
			XPathVersion defaultXPathVersion, 
			PolicyReferenceResolver referenceResolver,
			EvaluationContextHandler contextHandler) {
		super(validateFuncParamsAtRuntime,
				contextHandler,
				referenceResolver);
		this.defaultXPathVersion = defaultXPathVersion;
	}
	
	public RootEvaluationContext(boolean validateFuncParamsAtRuntime, 
			PolicyReferenceResolver referenceResolver, EvaluationContextHandler handler){
		this(validateFuncParamsAtRuntime, 
				XPathVersion.XPATH1, referenceResolver, handler);
	}

	@Override
	public XPathVersion getXPathVersion() { 
		return defaultXPathVersion;
	}	
}


