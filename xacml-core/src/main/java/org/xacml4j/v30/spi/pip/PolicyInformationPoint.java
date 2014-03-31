package org.xacml4j.v30.spi.pip;


import org.w3c.dom.Node;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;


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
	 * Resolves a content for a given attribute category
	 *
	 * @param context an evaluation context
	 * @param category an attribute category
	 * @return {@link Node} or {@code null}
	 * @throws Exception if an error occurs
	 */
	Node resolve(
			EvaluationContext context,
			CategoryId category)
		throws Exception;

	/**
	 * Gets resolver registry used by this PIP
	 *
	 * @return {@link ResolverRegistry}
	 */
	ResolverRegistry getRegistry();
}
