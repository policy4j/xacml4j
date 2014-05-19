package org.xacml4j.v30.pdp;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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
