package org.xacml4j.v30.spi.xpath;

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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.util.NodeNamespaceContext;

import com.google.common.base.Preconditions;

public class DefaultXPathProvider implements XPathProvider
{
	private final static Logger log = LoggerFactory.getLogger(DefaultXPathProvider.class);

	private XPathFactory xpathFactory;

	public DefaultXPathProvider(){
		this(XPathFactory.newInstance());
	}

	public DefaultXPathProvider(XPathFactory xpathFactory){
		Preconditions.checkNotNull(xpathFactory);
		this.xpathFactory = xpathFactory;
	}

	@Override
	public Node evaluateToNode(String path, Node context)
			throws XPathEvaluationException
	{
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNode XPath=\"{}\"", path);
			}
			XPath xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NodeNamespaceContext(context));
			Node result = (Node)xpath.evaluate(path, context, XPathConstants.NODE);
			if(log.isDebugEnabled() &&
					result != null){
				log.debug("Evaluation result=\"{}:{}\" node",
						result.getNamespaceURI(), result.getLocalName());

			}
			return result;
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw new XPathEvaluationException(path, context, e);
		}
	}

	@Override
	public NodeList evaluateToNodeSet(String path, Node context)
			throws XPathEvaluationException {
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNodeSet XPath=\"{}\"", path);
			}
			XPath xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NodeNamespaceContext(context));
			NodeList result = (NodeList)xpath.evaluate(path, context, XPathConstants.NODESET);
			if(log.isDebugEnabled() && result != null){
				log.debug("Evaluation result has=\"{}\" nodes",
						result.getLength());
			}
			return result;
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw new XPathEvaluationException(path, context, e);
		}
	}

	@Override
	public String evaluateToString(String path, Node context)
			throws XPathEvaluationException {
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToString XPath=\"{}\"", path);
			}
			XPath xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NodeNamespaceContext(context));
			return (String)xpath.evaluate(path, context, XPathConstants.STRING);
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw new XPathEvaluationException(path, context, e);
		}
	}

	@Override
	public Number evaluateToNumber(String path, Node context)
			throws XPathEvaluationException {
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNumber XPath=\"{}\"", path);
			}
			XPath xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NodeNamespaceContext(context));
			return (Number)xpath.evaluate(path, context, XPathConstants.NUMBER);
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw new XPathEvaluationException(path, context, e);
		}
	}

}
