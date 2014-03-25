package org.xacml4j.v30.pdp;

import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCode;

import com.google.common.base.Preconditions;


public class XPathEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = 1511624494955246280L;

	private String xpathExpression;

	public XPathEvaluationException(String xpath,
			StatusCode status,
			Throwable cause, 
			String message, Object... arguments) {
		super(status, cause, message, arguments);
		Preconditions.checkNotNull(xpath);
		this.xpathExpression = xpath;
	}
	
	public XPathEvaluationException(String xpath,
			String template, Object... arguments) {
		this(xpath, StatusCode.createProcessingError(), 
				null, template, arguments);
		
	}
	
	public XPathEvaluationException(String xpath, StatusCode status,
			String template, Object... arguments) {
		this(xpath, status, null, template, arguments);
		
	}

	public XPathEvaluationException(String xpath,
			Throwable cause, String message, Object... arguments) {
		this(xpath, StatusCode.createProcessingError(),cause, message, arguments);
	}
	
	

	public XPathEvaluationException(String xpath,
			Throwable cause) {
		this(xpath, StatusCode.createProcessingError(), cause, null);
	}

	public String getXPathExpression(){
		return xpathExpression;
	}
}
