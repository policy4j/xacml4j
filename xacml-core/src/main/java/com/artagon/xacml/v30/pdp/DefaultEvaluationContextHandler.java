package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.xpath.XPathEvaluationException;
import com.artagon.xacml.v30.spi.xpath.XPathProvider;
import com.artagon.xacml.v30.types.XPathExp;
import com.artagon.xacml.v30.types.XPathExpType;
import com.google.common.base.Preconditions;

class DefaultEvaluationContextHandler
	implements EvaluationContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(DefaultEvaluationContextHandler.class);

	private final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";

	private XPathProvider xpathProvider;
	private PolicyInformationPoint pip;

	private RequestContextCallback requestCallback;

	private Map<AttributeCategory, Node> contentCache;

	private Stack<AttributeDesignatorKey> designatorResolutionStack;
	private Stack<AttributeSelectorKey> selectorResolutionStack;
	private Stack<AttributeCategory> contentResolutionStack;

	DefaultEvaluationContextHandler(
			RequestContextCallback requestCallback,
			XPathProvider xpathProvider,
			PolicyInformationPoint pip)
	{
		Preconditions.checkNotNull(xpathProvider);
		Preconditions.checkNotNull(pip);
		Preconditions.checkNotNull(requestCallback);

		this.xpathProvider = xpathProvider;
		this.pip = pip;
		this.requestCallback = requestCallback;
		this.contentCache = new HashMap<AttributeCategory, Node>();
		this.selectorResolutionStack = new Stack<AttributeSelectorKey>();
		this.designatorResolutionStack = new Stack<AttributeDesignatorKey>();
		this.contentResolutionStack = new Stack<AttributeCategory>();
	}

	@Override
	public BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeDesignatorKey key)
		throws EvaluationException
	{

		Preconditions.checkNotNull(context);
		Preconditions.checkNotNull(key);
		BagOfAttributeExp v  = requestCallback.getAttributeValue(
				key.getCategory(),
				key.getAttributeId(),
				key.getDataType(),
				key.getIssuer());
		if(!v.isEmpty()){
			if(log.isDebugEnabled()){
				log.debug("Resolved designator=\"{}\" " +
						"from request to value=\"{}\"", key, v);
			}
			return v;
		}
		Preconditions.checkState(
				!designatorResolutionStack.contains(key),
				"Cyclic designator=\"%s\" resolution detected", key);
		try
		{
			designatorResolutionStack.push(key);
			v = pip.resolve(context, key);
			if(log.isDebugEnabled()){
				log.debug("Resolved designator=\"{}\" " +
						"from PIP to value=\"{}\"", key, v);
			}
			return v;
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new AttributeReferenceEvaluationException(
					context, key,
					StatusCode.createMissingAttributeError(), e);
		}finally{
			designatorResolutionStack.pop();
		}
	}


	@Override
	public BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeSelectorKey ref)
			throws EvaluationException
	{
		Preconditions.checkState(
				!selectorResolutionStack.contains(ref),
				"Cyclic designator=\"%s\" resolution detected", ref);
		try
		{
			selectorResolutionStack.push(ref);
			BagOfAttributeExp v =  doResolve(context, ref);
			if(log.isDebugEnabled()){
				log.debug("Resolved " +
						"selector=\"{}\" to bag=\"{}\"", ref, v);
			}
			return v;
		}finally{
			selectorResolutionStack.pop();
		}
	}

	@Override
	public final Node evaluateToNode(
			EvaluationContext context,
			String path,
			AttributeCategory categoryId)
		throws EvaluationException
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" " +
					"for category=\"{}\"", path, categoryId);
		}
		try
		{
			Node content = doGetContent(context, categoryId);
			if(content == null){
				return null;
			}
			return xpathProvider.evaluateToNode(
					context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
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
		try
		{
			Node content = doGetContent(context, categoryId);
			if(content == null){
				return null;
			}
			return xpathProvider.evaluateToNodeSet(context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
		}
	}

	@Override
	public final Number evaluateToNumber(EvaluationContext context,
			String path, AttributeCategory categoryId)
			throws EvaluationException
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" for category=\"{}\"", path, categoryId);
		}
		try
		{
			Node content = doGetContent(context, categoryId);
			if(content == null){
				return null;
			}
			return xpathProvider.evaluateToNumber(context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
		}
	}

	@Override
	public final String evaluateToString(EvaluationContext context,
			String path, AttributeCategory categoryId)
			throws EvaluationException
	{
		if(log.isDebugEnabled()){
			log.debug("Evaluating xpath=\"{}\" " +
					"for category=\"{}\"", path, categoryId);
		}
		try
		{
			Node content = doGetContent(context, categoryId);
			if(content == null){
				return null;
			}
			return xpathProvider.evaluateToString(
					context.getXPathVersion(), path, content);
		}catch(XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new com.artagon.xacml.v30.pdp.XPathEvaluationException(path, context, e);
		}
	}

	/**
	 * Resolves category content via {@link PolicyInformationPoint}
	 *
	 * @param context an evaluation context
	 * @param ref an attribute reference
	 * @return {@link BagOfAttributeExp}
	 * @exception Exception
	 */
	private final Node doGetContent(EvaluationContext context, AttributeCategory category)
		throws Exception
	{

		Node content = requestCallback.getContent(category);
		if(content != null){
			if(log.isDebugEnabled()){
				log.debug("Resolved content=\"{}\" from a request",
						content);
			}
			return content;
		}
		if(contentCache.containsKey(category)){
			content = contentCache.get(category);
			if(log.isDebugEnabled()){
				log.debug("Resolved content=\"{}\" " +
						"from cache", content);
			}
			return content;
		}
		Preconditions.checkState(
				!contentResolutionStack.contains(category));
		try
		{
			contentResolutionStack.push(category);
			content = pip.resolve(context, category);
			if(log.isDebugEnabled()){
				log.debug("Resolved content=\"{}\" " +
						"from PIP", content);
			}
			contentCache.put(category, content);
			return content;
		}finally{
			contentResolutionStack.pop();
		}
	}

	private final BagOfAttributeExp doResolve(
			EvaluationContext context,
			AttributeSelectorKey ref) throws EvaluationException
	{
		try
		{
			Node content = doGetContent(context, ref.getCategory());
			if(content == null){
				return ref.getDataType().bagType().createEmpty();
			}
			Node contextNode = content;
			BagOfAttributeExp v = requestCallback.getAttributeValue(
					ref.getCategory(),
						(ref.getContextSelectorId() == null?CONTENT_SELECTOR:ref.getContextSelectorId()),
								XPathExpType.XPATHEXPRESSION, null);
			if(v.size() > 1){
				throw new AttributeReferenceEvaluationException(context, ref,
						"Found more than one value of=\"%s\"", ref.getContextSelectorId());
			}
			if(v.size() == 1){
				XPathExp xpath = v.value();
				if(xpath.getCategory() != ref.getCategory()){
					throw new AttributeReferenceEvaluationException(context, ref,
							"AttributeSelector category=\"%s\" and " +
							"ContextAttributeId XPathExpression " +
							"category=\"%s\" do not match", ref.getCategory(),
							xpath.getCategory());
				}
				if(log.isDebugEnabled()){
					log.debug("Evaluating " +
							"contextSelector xpath=\"{}\"", xpath.getValue());
				}
				contextNode = xpathProvider.evaluateToNode(
						context.getXPathVersion(), xpath.getPath(), content);
			}
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(
					context.getXPathVersion(), ref.getPath(), contextNode);
			if(nodeSet == null ||
					nodeSet.getLength() == 0){
				log.debug("Selected nodeset via xpath=\"{}\" and category=\"{}\" is empty",
						ref.getPath(), ref.getCategory());
				return (BagOfAttributeExp)ref.getDataType().bagType().createEmpty();
			}
			if(log.isDebugEnabled()){
				log.debug("Found=\"{}\" nodes via xpath=\"{}\" and category=\"{}\"",
						new Object[]{nodeSet.getLength(), ref.getPath(), ref.getCategory()});
			}
			return toBag(context, ref, nodeSet);
		}
		catch(XPathEvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new AttributeReferenceEvaluationException(context, ref,
					StatusCode.createProcessingError(), e);
		}
		catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw e;
		}
		catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw new AttributeReferenceEvaluationException(
					context, ref,
					StatusCode.createProcessingError(), e);
		}
	}

	/**
	 * Converts a given node list to the {@link BagOfAttributeExp}
	 *
	 * @param context an evaluation context
	 * @param ref an attribute selector
	 * @param nodeSet a node set
	 * @return {@link BagOfAttributeExp}
	 * @throws EvaluationException
	 */
	private BagOfAttributeExp toBag(
			EvaluationContext context,
			AttributeSelectorKey ref, NodeList nodeSet)
		throws EvaluationException
	{
		Collection<AttributeExp> values = new LinkedList<AttributeExp>();
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
			try
			{
				AttributeExp value = ref.getDataType().fromXacmlString(v);
				if(log.isDebugEnabled()){
					log.debug("Node of type=\"{}\" " +
							"converted attribute=\"{}\"", n.getNodeType(), value);
				}
				values.add(value);
			}catch(Exception e){
				throw new AttributeReferenceEvaluationException(
						context,
						ref,
						StatusCode.createSyntaxError(), e);
			}
		}
	  	return (BagOfAttributeExp) ref.getDataType().bagType().create(values);
	}
}
