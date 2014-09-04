package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;


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
			DecisionRuleEvaluationContext context,
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
			DecisionRuleEvaluationContext context,
			CategoryId category)
		throws Exception;

	/**
	 * Gets resolver registry used by this PIP
	 *
	 * @return {@link ResolverRegistry}
	 */
	ResolverRegistry getRegistry();
}
