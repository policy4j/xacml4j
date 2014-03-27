package org.xacml4j.v30.pdp;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.types.XPathExp;

public interface EvaluationContextHandler
{
	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeDesignatorKey key) throws EvaluationException;

	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeSelectorKey key) throws EvaluationException;
	
	Node evaluateToNode(EvaluationContext context, XPathExp xpath) 
			throws XPathEvaluationException;
	NodeList evaluateToNodeSet(EvaluationContext context, XPathExp xpath)
			throws XPathEvaluationException;
	Number evaluateToNumber(EvaluationContext context, XPathExp xpath)
			throws XPathEvaluationException;
	String evaluateToString(EvaluationContext context, XPathExp xpath)
			throws XPathEvaluationException;
}
