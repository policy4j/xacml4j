package com.artagon.xacml.v3.policy;

import javax.xml.xpath.XPathExpressionException;

public class XPathEvaluationException extends EvaluationException
{
	public XPathEvaluationException(XPathExpressionException e){
		super(e);
	}
}
