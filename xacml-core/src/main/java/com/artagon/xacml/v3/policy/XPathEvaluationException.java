package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.StatusCode;

public class XPathEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 1511624494955246280L;
	
	private String xpathExpression;
	
	public XPathEvaluationException(String xpathExpression, 
			EvaluationContext context,
			String template, Object... arguments) {
		super(StatusCode.createProcessingError(), 
				context, template, arguments);
		this.xpathExpression = xpathExpression;
	}

	public XPathEvaluationException(String xpathExpression, 
			EvaluationContext context,
			Throwable cause, String message, Object... arguments) {
		super(StatusCode.createProcessingError(), 
				context, cause, message, arguments);
		this.xpathExpression = xpathExpression;
	}

	public XPathEvaluationException(String xpathExpression,
			EvaluationContext context,
			Throwable cause) {
		super(StatusCode.createProcessingError(), 
				context, cause);
		this.xpathExpression = xpathExpression;
	}
	
	public String getXPathExpression(){
		return xpathExpression;
	}
	
}
