package com.artagon.xacml.v30.spi.pip;


import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.core.AttributeCategory;

/**
 * A XACML Policy Information Point
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyInformationPoint 
{
	/**
	 * Gets identifier for this policy 
	 * information point
	 * 
	 * @return a unique identifier
	 */
	String getId();
	
	/**
	 * Resolves a given {@link AttributeDesignatorKey}
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute designator
	 * @return {@link BagOfAttributesExp}
	 * @throws Exception if an error occurs
	 */
	BagOfAttributesExp resolve(
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
