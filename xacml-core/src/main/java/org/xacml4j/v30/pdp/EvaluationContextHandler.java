package org.xacml4j.v30.pdp;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;



public interface EvaluationContextHandler 
{
	NodeList evaluateToNodeSet(
			EvaluationContext context,
			String xpath, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	
	String evaluateToString(
			EvaluationContext context,
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	Node evaluateToNode(
			EvaluationContext context,
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	Number evaluateToNumber(
			EvaluationContext context,
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeDesignatorKey key) throws EvaluationException;
	
	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeSelectorKey key) throws EvaluationException;
}
