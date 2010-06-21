package com.artagon.xacml.v3.spi.xpath;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.util.NodeNamespaceContext;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.spi.XPathEvaluationException;
import com.artagon.xacml.v3.spi.XPathProvider;
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
	public Node evaluateToNode(XPathVersion v, String path, Node context)
			throws XPathEvaluationException 
	{
		Preconditions.checkArgument(v != null);
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{

			if(!v.equals(XPathVersion.XPATH1)){
				throw new XPathEvaluationException(
						"Unsupported XPath version", path, v, context);
			}
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
			throw new XPathEvaluationException(path, v, context, e);
		}
	}

	@Override
	public NodeList evaluateToNodeSet(XPathVersion v, String path, Node context)
			throws XPathEvaluationException {
		Preconditions.checkArgument(v != null);
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{
			if(!v.equals(XPathVersion.XPATH1)){
				throw new XPathEvaluationException(
						"Unsupported XPath version", path, v, context);
			}
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
			throw new XPathEvaluationException(path, v, context, e);
		}
	}

	@Override
	public String evaluateToString(XPathVersion v, String path, Node context)
			throws XPathEvaluationException {
		Preconditions.checkArgument(v != null);
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{
			if(!v.equals(XPathVersion.XPATH1)){
				throw new XPathEvaluationException(
						"Unsupported XPath version", path, v, context);
			}
			if(log.isDebugEnabled()){
				log.debug("EvaluateToString XPath=\"{}\"", path);
			}
			XPath xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NodeNamespaceContext(context));
			return (String)xpath.evaluate(path, context, XPathConstants.STRING);
		}catch(XPathExpressionException e){
			throw new XPathEvaluationException(path, v, context, e);
		}
	}
	
	@Override
	public Number evaluateToNumber(XPathVersion v, String path, Node context)
			throws XPathEvaluationException {
		Preconditions.checkArgument(v != null);
		Preconditions.checkArgument(path != null);
		Preconditions.checkArgument(context != null);
		try
		{
			if(!v.equals(XPathVersion.XPATH1)){
				throw new XPathEvaluationException(
						"Unsupported XPath version", path, v, context);
			}
			if(log.isDebugEnabled()){
				log.debug("EvaluateToNumber XPath=\"{}\"", path);
			}
			XPath xpath = xpathFactory.newXPath();
			xpath.setNamespaceContext(new NodeNamespaceContext(context));
			return (Number)xpath.evaluate(path, context, XPathConstants.NUMBER);
		}catch(XPathExpressionException e){
			throw new XPathEvaluationException(path, v, context, e);
		}
	}
	
}
