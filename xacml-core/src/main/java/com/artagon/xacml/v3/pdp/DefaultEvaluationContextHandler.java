package com.artagon.xacml.v3.pdp;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextHandler;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.ResolutionScope;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.XPathEvaluationException;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XPathExpressionValue;
import com.google.common.base.Preconditions;

class DefaultEvaluationContextHandler implements EvaluationContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(DefaultEvaluationContextHandler.class);
	
	private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
	
	private XPathProvider xpathProvider;
	private PolicyInformationPoint pip;
	
	private RequestContextCallback requestContextCallback;
	
	private Map<AttributeDesignator, BagOfAttributeValues> attributeDesignatorCache;
	private Map<AttributeSelector, BagOfAttributeValues> attributeSelectorCache;
	private Map<AttributeCategory, Node> contentCache;
	
	DefaultEvaluationContextHandler(
			RequestContextCallback requestContextCallback,
			XPathProvider xpathProvider, 
			PolicyInformationPoint pip)
	{
		Preconditions.checkNotNull(xpathProvider);
		Preconditions.checkNotNull(pip);
		Preconditions.checkNotNull(requestContextCallback);
		this.xpathProvider = xpathProvider;
		this.pip = pip;
		this.requestContextCallback = requestContextCallback;
		this.attributeDesignatorCache = new HashMap<AttributeDesignator, BagOfAttributeValues>();
		this.attributeSelectorCache = new HashMap<AttributeSelector, BagOfAttributeValues>();
		this.contentCache = new HashMap<AttributeCategory, Node>();
	}
	

	@Override
	public BagOfAttributeValues resolve(EvaluationContext context, AttributeDesignator ref) 
		throws EvaluationException 
	{
		BagOfAttributeValues v = requestContextCallback.getAttributeValues(
				ref.getCategory(), 
				ref.getAttributeId(), 
				ref.getDataType(), 
				ref.getIssuer());
		if(!v.isEmpty() || 
				(context.getResolutionScope() 
						== ResolutionScope.REQUEST)){
			return v;
		}
		v = attributeDesignatorCache.get(ref);
		if(v != null){
			if(log.isDebugEnabled()){
				log.debug("Resolved " +
						"designator=\"{}\" from the cache=\"{}\"", ref, v);
			}
			return v;
		}
		v =  pip.resolve(context, ref, requestContextCallback);
		attributeDesignatorCache.put(ref, v);
		return v;
	}


	@Override
	public BagOfAttributeValues resolve(
			EvaluationContext context, AttributeSelector ref)
			throws EvaluationException 
	{
		BagOfAttributeValues v = attributeSelectorCache.get(ref);
		if(v != null){
			if(log.isDebugEnabled()){
				log.debug("Resolved " +
						"selector=\"{}\" from the cache=\"{}\"", ref, v);
			}
			return v;
		}
		v =  doResolve(context, ref);
		attributeSelectorCache.put(ref, v);
		return v;
	}
	
	@Override
	public final Node evaluateToNode(EvaluationContext context, 
			String path, AttributeCategory categoryId)
			throws EvaluationException 
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		Node content = doGetContent(context, categoryId);
		if(content == null){
			log.debug("Content is not available for category=\"{}\"", categoryId);
			return null;
		}
		try{
			return xpathProvider.evaluateToNode(
					context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			log.debug("Received exception while evaluating xpath", e);
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, context, e);
		}
	}

	@Override
	public final NodeList evaluateToNodeSet(
			EvaluationContext context, 
			String path, 
			AttributeCategory categoryId)
			throws EvaluationException 
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		Node content = doGetContent(context, categoryId);
		if(content == null){
			log.debug("Content is not available for category=\"{}\"", categoryId);
			return null;
		}
		try{
			return xpathProvider.evaluateToNodeSet(context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, context, e);
		}
	}

	@Override
	public final Number evaluateToNumber(EvaluationContext context, 
			String path, AttributeCategory categoryId)
			throws EvaluationException {
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		Node content = doGetContent(context, categoryId);
		if(content == null){
			return null;
		}
		try{
			return xpathProvider.evaluateToNumber(context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, context, e);
		}
	}
	
	@Override
	public final String evaluateToString(EvaluationContext context, 
			String path, AttributeCategory categoryId)
			throws EvaluationException {
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" " +
					"for category=\"{}\"", path, categoryId);
		}
		Node content = doGetContent(context, categoryId);
		if(content == null){
			return null;
		}
		try{
			return xpathProvider.evaluateToString(
					context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			throw new com.artagon.xacml.v3.XPathEvaluationException(path, context, e);
		}
	}

	/**
	 * Resolves category content via {@link PolicyInformationPoint}
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute reference
	 * @return {@link BagOfAttributeValues}
	 * @throws EvaluationException if an error occurs
	 * while resolving reference
	 */
	private final Node doGetContent(EvaluationContext context, AttributeCategory category) 
		throws EvaluationException 
	{
		
		Node content = requestContextCallback.getContent(category);
		if(content != null){
			if(log.isDebugEnabled()){
				log.debug("Resolved content=\"{}\" from a request", 
						content);
			}
			return content;
		}
		ResolutionScope scope = context.getResolutionScope();
		if(log.isDebugEnabled()){
			log.debug("Request to get content, scope=\"{}\" " +
					"category=\"{}\"", scope, category);
		}
		if(scope == ResolutionScope.REQUEST){
			return null;
		}
		if(contentCache.containsKey(category)){
			content = contentCache.get(category);
			if(log.isDebugEnabled()){
				log.debug("Resolved content=\"{}\" " +
						"from cache", content);
			}
			return content;
		}
		content = pip.resolve(context, category, requestContextCallback);
		if(log.isDebugEnabled()){
			log.debug("Resolved content=\"{}\" " +
					"from PIP", content);
		}
		contentCache.put(category, content);
		return content;
	}

	
	private final BagOfAttributeValues doResolve(
			EvaluationContext context,
			AttributeSelector ref) throws EvaluationException {
		try
		{
			Node content = doGetContent(context, ref.getCategory());
			if(content == null){
				return ref.getDataType().bagType().createEmpty();
			}
			Node contextNode = content;
			BagOfAttributeValues v = requestContextCallback.getAttributeValues(ref.getCategory(), 
						(ref.getContextSelectorId() == null?CONTENT_SELECTOR:ref.getContextSelectorId()), 
								XPathExpressionType.XPATHEXPRESSION);
			if(v.size() > 1){
				throw new AttributeReferenceEvaluationException(context, ref, 
						"Found more than one value of=\"%s\"", ref.getContextSelectorId());
			}
			if(v.size() == 1){
				if(log.isDebugEnabled()){
					log.debug("Found ContextSelector attribute");
				}
				XPathExpressionValue xpath = v.value();
				if(xpath.getCategory() != ref.getCategory()){
					throw new AttributeReferenceEvaluationException(context, ref, 
							"AttributeSelector category=\"%s\" and " +
							"ContextAttributeId category=\"%s\" do not match", ref.getCategory(), 
							xpath.getCategory());
				}
				if(log.isDebugEnabled()){
					log.debug("Evaluating " +
							"contextSelector xpath=\"{}\"", xpath.getValue());
				}
				contextNode = xpathProvider.evaluateToNode(
						context.getXPathVersion(), xpath.getValue(), content);
			}
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(
					context.getXPathVersion(), ref.getPath(), contextNode);
			if(nodeSet == null || 
					nodeSet.getLength() == 0){
				log.debug("Selected nodeset via xpath=\"{}\" and category=\"{}\" is empty", 
						ref.getPath(), ref.getCategory());
				return (BagOfAttributeValues)ref.getDataType().bagType().createEmpty();
			}
			if(log.isDebugEnabled()){
				log.debug("Found=\"{}\" nodes via xpath=\"{}\" and category=\"{}\"", 
						new Object[]{nodeSet.getLength(), ref.getPath(), ref.getCategory()});
			}
			return toBag(context, ref, nodeSet);
		}
		catch(XPathEvaluationException e){
			throw new AttributeReferenceEvaluationException(context, ref, 
					StatusCode.createProcessingError(), e);
		}
	}
	
	/**
	 * Converts a given node list to the {@link BagOfAttributeValues}
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute selector
	 * @param nodeSet a node set
	 * @return {@link BagOfAttributeValues}
	 * @throws EvaluationException
	 */
	private BagOfAttributeValues toBag(EvaluationContext context,
			AttributeSelector ref, NodeList nodeSet) 
		throws EvaluationException
	{
		Collection<AttributeValue> values = new LinkedList<AttributeValue>();
		for(int i = 0; i< nodeSet.getLength(); i++)
		{
			Node n = nodeSet.item(i);
			String v = null;
			switch(n.getNodeType()){
				case Node.TEXT_NODE:
					v = ((Text)n).getData();
					break;
				case Node.PROCESSING_INSTRUCTION_NODE:
					v = ((ProcessingInstruction)n).getData();
					break;
				case Node.ATTRIBUTE_NODE:
					v = ((Attr)n).getValue();
					break;
				case Node.COMMENT_NODE:
					v = ((Comment)n).getData();
					break;
				default:
					throw new AttributeReferenceEvaluationException(
							StatusCode.createSyntaxError(),
							context, ref, 
							"Unsupported DOM node type=\"%d\"", n.getNodeType());
			}
			try{
				values.add(ref.getDataType().fromXacmlString(v));
			}catch(Exception e){
				throw new AttributeReferenceEvaluationException(context, 
						ref, StatusCode.createProcessingError(), e);
			}
		}
	  	return (BagOfAttributeValues) ref.getDataType().bagType().create(values);
	}
}
