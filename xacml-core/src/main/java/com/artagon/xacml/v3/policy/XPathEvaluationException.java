package com.artagon.xacml.v3.policy;

import javax.xml.xpath.XPathExpressionException;

public class XPathEvaluationException extends EvaluationException
{
	private static final long serialVersionUID = -485015612384300131L;

	public XPathEvaluationException(XPathExpressionException e){
		super(e);
	}
}
