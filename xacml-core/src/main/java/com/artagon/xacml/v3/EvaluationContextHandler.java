package com.artagon.xacml.v3;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	
	/**
	 * Resolves given {@link AttributeDesignator} to a
	 * {@link BagOfAttributeValues} instance
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute designator
	 * @return {@link BagOfAttributeValues} instance, 
	 * never returns <code>null</code>. If designator
	 * can not be resolved returns an empty bag
	 * @throws EvaluationException if an evaluation 
	 * error occurs
	 */
	BagOfAttributeValues resolve(
			EvaluationContext context,
			AttributeDesignator ref) throws EvaluationException;
	
	BagOfAttributeValues resolve(
			EvaluationContext context,
			AttributeSelector selector) throws EvaluationException;
}
