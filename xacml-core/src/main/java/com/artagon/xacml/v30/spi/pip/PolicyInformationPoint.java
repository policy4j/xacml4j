package com.artagon.xacml.v30.spi.pip;


import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.pdp.AttributeDesignatorKey;
import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.pdp.EvaluationContext;

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
	 * @return {@link BagOfAttributeExp}
	 * @throws Exception if an error occurs
	 */
	BagOfAttributeExp resolve(
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
