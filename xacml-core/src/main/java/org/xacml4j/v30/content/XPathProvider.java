package org.xacml4j.v30.content;

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

import java.util.Objects;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xacml4j.util.DOMUtil;
import org.xacml4j.util.NodeNamespaceContext;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.XPathVersion;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

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
			throws PathEvaluationException
	{
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		if(log.isDebugEnabled()){
			log.debug("EvaluateToNode XPath=\"{}\"", path);
		}
		XPathExpression xpath = newXPath(path, context);
		try
		{
			Node result = (Node)xpath.evaluate(context, XPathConstants.NODE);
			if(result != null){
				log.debug("Xpath={} result node name=\"{}\" node type=\"{}\"",
						  xpath,
						  DOMUtil.toString(result),
				          result.getNodeType());

			}else{
				log.warn("Xpath={} result null", xpath);
			}
			return result;
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw PathEvaluationException.invalidXpath(path, e);
		}
	}

	default NodeList evaluateToNodeSet(String path, Node context)
			throws PathEvaluationException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		if(log.isDebugEnabled()){
			log.debug("EvaluateToNodeSet XPath=\"{}\"", path);
		}
		XPathExpression xpath = newXPath(path, context);
		try
		{
			NodeList result = (NodeList)xpath.evaluate(context, XPathConstants.NODESET);
			if(result != null){
				log.debug("Evaluation result has=\"{}\" nodes",
				          result.getLength());
				if(log.isDebugEnabled()){
					for(int i = 0; i < result.getLength(); i++){
						Node n = result.item(i);
						log.debug("Result at index=\"{}\" domType=\"{}\" domNodeType=\"{}\"", i,
						          DOMUtil.toString(n), n.getNodeType());
					}
				}
			}else {
				log.warn("XPath={} result is null", path);
			}
			return result;
		}
		catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw PathEvaluationException.invalidXpath(path, e);
		}
	}

	default String evaluateToString(String path, Node context)
			throws PathEvaluationException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		if(log.isDebugEnabled()){
			log.debug("EvaluateToString XPath=\"{}\"", path);
		}
		XPathExpression xpath = newXPath(path, context);
		try
		{
			String result = (String)xpath.evaluate(context, XPathConstants.STRING);
			if(log.isDebugEnabled()){
				log.debug("EvaluateToString XPath=\"{}\" result=\"{}\"", path, result);
			}
			return result;
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw PathEvaluationException.invalidXpath(path, e);
		}
	}

	default Number evaluateToNumber(String path, Node context)
			throws PathEvaluationException {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(context, "context");
		if(log.isDebugEnabled()){
			log.debug("EvaluateToNumber XPath=\"{}\"", path);
		}
		XPathExpression xpath = newXPath(path, context);
		try
		{
			Number result = (Number)xpath.evaluate(context, XPathConstants.NUMBER);
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNumber XPath=\"{}\" result=\"{}\"", path, result);
			}
			return result;
		}catch(XPathExpressionException e){
			if(log.isDebugEnabled()){
				log.debug(path, e);
			}
			throw PathEvaluationException
					.invalidXpath(path, e);
		}
	}

	static XPathProvider defaultProvider(){
		return DefaultXPathProvider.INSTANCE;
	}

	/**
	 * @see {@link <a href="http://leakfromjavaheap.blogspot.com/2014/12/xpath-evaluation-performance-tweaks.html"/>}
	 */
	final class DefaultXPathProvider implements XPathProvider
	{
		private final static XPathProvider INSTANCE = new DefaultXPathProvider();

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


		private DefaultXPathProvider(){
			this(()->XPATH_FACTORY.get());
		}

		private DefaultXPathProvider(Supplier<XPathFactory> xpathFactory){
			Preconditions.checkNotNull(xpathFactory);
			this.xpathFactory = Objects.requireNonNull(xpathFactory,
					"xpathFactorySupplier");
		}

		public XPathExpression newXPath(String xpath, Node node){
			XPath xp = DefaultXPathProvider.XPATH_FACTORY.get().newXPath();
			xp.setNamespaceContext(new NodeNamespaceContext(node));
			try{
				return xp.compile(xpath);
			}catch(XPathExpressionException e){
				throw PathEvaluationException.invalidXpath(xpath, e);
			}
		}
	}
}
