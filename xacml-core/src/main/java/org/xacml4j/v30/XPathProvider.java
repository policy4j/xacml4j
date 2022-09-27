package org.xacml4j.v30;

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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.util.NodeNamespaceContext;

import javax.xml.xpath.*;
import java.util.Objects;

/**
 * An XPath provider for executing XPath expressions
 *
 * @author Giedrius Trumpickas
 */
public interface XPathProvider
{
	Logger log = LoggerFactory.getLogger(XPathProvider.class);

	/**
	 * Gets provider XPATH version
	 *
	 * @return provider XPATH version
	 */
	default XPathVersion getPathVersion(){
		return XPathVersion.XPATH1;
	}

	XPathExpression newXPath(String xpath, Node node);

	default Node evaluateToNode(String path, Node context)
			throws XPathEvaluationException
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNode XPath=\"{}\"", path);
			}
			XPathExpression xpath = newXPath(path, context);
			Node result = (Node)xpath.evaluate(context, XPathConstants.NODE);
			if(log.isDebugEnabled() &&
					result != null){
				log.debug("Result node name=\"{}:{}\" node type=\"{}\"",
				          result.getLocalName(), result.getNamespaceURI(),
				          result.getNodeType());

			}
			return result;
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw XPathEvaluationException
					.wrap(path, context, e);
		}
	}

	default NodeList evaluateToNodeSet(String path, Node context)
			throws XPathEvaluationException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNodeSet XPath=\"{}\"", path);
			}
			XPathExpression xpath = newXPath(path, context);
			NodeList result = (NodeList)xpath.evaluate(context, XPathConstants.NODESET);
			if(log.isDebugEnabled() && result != null){
				log.debug("Evaluation result has=\"{}\" nodes",
						result.getLength());
				for(int i = 0; i < result.getLength(); i++){
					Node n = result.item(i);
					log.debug("Result at index=\"{}\" name=\"{}:{}\" type=\"{}\"", i,
					          n.getLocalName(), n.getNamespaceURI(),
					          n.getNodeType());
				}
			}
			return result;
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw XPathEvaluationException
					.wrap(path, context, e);
		}
	}

	default String evaluateToString(String path, Node context)
			throws XPathEvaluationException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToString XPath=\"{}\"", path);
			}
			XPathExpression xpath = newXPath(path, context);
			String result = (String)xpath.evaluate(context, XPathConstants.STRING);
			if(log.isDebugEnabled()){
				log.debug("EvaluateToString XPath=\"{}\" result=\"{}\"", path, result);
			}
			return result;
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw XPathEvaluationException
					.wrap(path, context, e);
		}
	}

	default Number evaluateToNumber(String path, Node context)
			throws XPathEvaluationException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		try
		{
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNumber XPath=\"{}\"", path);
			}
			XPathExpression xpath = newXPath(path, context);
			Number result = (Number)xpath.evaluate(context, XPathConstants.NUMBER);
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNumber XPath=\"{}\" result=\"{}\"", path, result);
			}
			return result;
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw XPathEvaluationException
					.wrap(path, context, e);
		}
	}

	static XPathProvider defaultProvider(){
		return DefaultJDK.INSTANCE;
	}

	/**
	 * @see {@link <a href="http://leakfromjavaheap.blogspot.com/2014/12/xpath-evaluation-performance-tweaks.html"/>}
	 */
	final class DefaultJDK implements XPathProvider
	{
		private final static XPathProvider INSTANCE = new DefaultJDK();

		/**
		 * Magic properties for XPath performance
		 */
		private static final String DTM_MANAGER_NAME = "com.sun.org.apache.xml.internal.dtm.DTMManager";
		private static final String DTM_MANAGER_VALUE = "com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault";

		static {
			System.setProperty(DTM_MANAGER_NAME, DTM_MANAGER_VALUE);
		}

		/**
		 * {@link XPathFactory#newInstance()} is very expensive operation
		 * and {@link XPathFactory} is not threads safe
		 */
		private static final ThreadLocal<XPathFactory> XPATH_FACTORY =
				ThreadLocal.withInitial(()->{
					log.debug("Creating new XPath instance for thread id=\"{}\" name=\"{}\"", Thread.currentThread().getId(),
					          Thread.currentThread().getName());
					return XPathFactory.newInstance();});

		private Supplier<XPathFactory> xpathFactory;


		private DefaultJDK(){
			this(()->XPATH_FACTORY.get());
		}

		private DefaultJDK(Supplier<XPathFactory> xpathFactory){
			Preconditions.checkNotNull(xpathFactory);
			this.xpathFactory = Objects.requireNonNull(xpathFactory,
					"xpathFactorySupplier");
		}

		public XPathExpression newXPath(String xpath, Node node){
			XPath xp = DefaultJDK.XPATH_FACTORY.get().newXPath();
			xp.setNamespaceContext(new NodeNamespaceContext(node));
			try{
				return xp.compile(xpath);
			}catch(XPathExpressionException e){
				throw PathEvaluationException
						.wrap(e);
			}
		}
	}
}