package org.xacml4j.v30.spi.xpath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XPath provider for executing XPath expressions
 *
 * @author Giedrius Trumpickas
 */
public interface XPathProvider
{
	NodeList evaluateToNodeSet(String path, Node context)
		throws XPathEvaluationException;

	String evaluateToString(String path, Node context)
		throws XPathEvaluationException;

	Node evaluateToNode(String path, Node context)
		throws XPathEvaluationException;

	Number evaluateToNumber(String path, Node context)
		throws XPathEvaluationException;

}
