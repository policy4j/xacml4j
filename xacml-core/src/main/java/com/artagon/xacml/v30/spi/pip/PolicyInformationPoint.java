package com.artagon.xacml.v30.spi.pip;


import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.EvaluationContext;

/**
 * A XACML Policy Information Point
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyInformationPoint 
{
	/**
	 * Resolves a given {@link AttributeDesignatorKey}
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute designator
	 * @return {@link BagOfAttributeValues}
	 * @throws Exception if an error occurs
	 */
	BagOfAttributeValues resolve(
			EvaluationContext context,
			AttributeDesignatorKey ref) 
		throws Exception;
	
	
	/**
	 * Resolves a content for a given
	 * attribute category
	 * 
	 * @param context an evaluation context
	 * @param category an attribute category
	 * @return {@link Node} or <code>null</code>
	 * @throws Exception if an error occurs
	 */
	Node resolve(
			EvaluationContext context,
			AttributeCategory category) 
		throws Exception;
}
