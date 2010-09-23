package com.artagon.xacml.v3;

import org.w3c.dom.Node;

public interface EvaluationContextHandler 
{
	/**
	 * Gets content for a given attribute category
	 * 
	 * @param categoryId an attribute category
	 * @return an instance of {@link Node} or
	 * <code>null</code> if content is not available
	 * for a given category 
	 */
	Node getContent(EvaluationContext context, AttributeCategoryId categoryId);
	
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
