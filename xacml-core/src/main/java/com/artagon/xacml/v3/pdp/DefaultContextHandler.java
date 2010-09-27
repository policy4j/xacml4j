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
import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeResolutionScope;
import com.artagon.xacml.v3.AttributeSelector;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextHandler;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.XPathEvaluationException;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XPathExpressionValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class DefaultContextHandler implements EvaluationContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(DefaultContextHandler.class);
	
	final static String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
	
	private RequestContext request;
	private XPathProvider xpathProvider;
	private PolicyInformationPoint pip;
	
	/* Request scope attribute designator resolution cache */
	private Map<AttributeDesignator, BagOfAttributeValues> attributeDesignatorCache;
	
	/* Request scope attribute selector resolution cache */
	private Map<AttributeSelector, BagOfAttributeValues> attributeSelectorCache;
	
	/* Request scope attribute selector resolution cache */
	private Map<AttributeCategory, Node> contentCache;
	
	public DefaultContextHandler(XPathProvider xpathProvider, 
			RequestContext request, PolicyInformationPoint pip)
	{
		Preconditions.checkNotNull(request);
		Preconditions.checkArgument(!request.hasRepeatingCategories());
		Preconditions.checkNotNull(xpathProvider);
		Preconditions.checkNotNull(pip);
		this.request = request;
		this.xpathProvider = xpathProvider;
		this.pip = pip;
		this.attributeDesignatorCache = new HashMap<AttributeDesignator, BagOfAttributeValues>();
		this.attributeSelectorCache = new HashMap<AttributeSelector, BagOfAttributeValues>();
		this.contentCache = new HashMap<AttributeCategory, Node>();
	}
	
	
	@Override
	public Node getContent(EvaluationContext context,
			AttributeCategory categoryId) 
	{
		Node content = contentCache.get(categoryId);
		if(content == null)
		{
			content = doGetContent(context, categoryId);
			if(content != null){
				contentCache.put(categoryId, content);
			}
		}
		return content;
	}


	@Override
	public BagOfAttributeValues resolve(EvaluationContext context, AttributeDesignator ref) 
		throws EvaluationException 
	{
		Collection<AttributeValue> values = request.getAttributeValues(ref.getCategory(), 
				ref.getAttributeId(), ref.getIssuer(), ref.getDataType());
		if(!values.isEmpty()){
			return ref.getDataType().bagType().create(values);
		}
		if(context.getAttributeResolutionScope() == AttributeResolutionScope.REQUEST){
			return ref.getDataType().bagType().createEmpty();
		}
		return doResolve(context, ref);
	}


	@Override
	public BagOfAttributeValues resolve(
			EvaluationContext context, AttributeSelector ref)
			throws EvaluationException {
		
		BagOfAttributeValues v = attributeSelectorCache.get(ref);
		if(v == null){
			v = doResolve(context, ref);
			if(v != null){
				attributeSelectorCache.put(ref, v);
			}
		}
		return v;
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
	{
		Attributes attr = request.getOnlyAttributes(category);
		if(attr == null || 
				attr.getContent() == null){
			if(context.getAttributeResolutionScope() == 
				AttributeResolutionScope.REQUEST_EXTERNAL)
			return pip.resolve(context, category, new DefaultRequestAttributesCallback());
		}
		return (attr != null)?attr.getContent():null;
	}

	/**
	 * Resolves attribute via {@link PolicyInformationPoint}
	 * 
	 * @param context an evaluation context
	 * @param ref an attribute reference
	 * @return {@link BagOfAttributeValues}
	 * @throws EvaluationException if an error occurs
	 * while resolving reference
	 */
	private final BagOfAttributeValues doResolve(
			EvaluationContext context,
			AttributeDesignator ref) throws EvaluationException 
	{
		BagOfAttributeValues v = attributeDesignatorCache.get(ref);
		if(v == null){
			v = pip.resolve(context, ref, new DefaultRequestAttributesCallback());
			attributeDesignatorCache.put(ref, v);
		}
		return v;
	}
	
	private final BagOfAttributeValues doResolve(
			EvaluationContext context,
			AttributeSelector ref) throws EvaluationException {
		try
		{
			Node content = getContent(context, ref.getCategory());
			if(content == null){
				if(context.getAttributeResolutionScope() == AttributeResolutionScope.REQUEST_EXTERNAL){
					content = pip.resolve(context, ref.getCategory(), 
							new DefaultRequestAttributesCallback());
				}
			}
			if(content == null){
				return ref.getDataType().bagType().createEmpty();
			}
			Node contextNode = content;
			Collection<AttributeValue> v = request.getAttributeValues(ref.getCategory(), 
						ref.getContextSelectorId() == null?CONTENT_SELECTOR:ref.getContextSelectorId(), 
								XPathExpressionType.XPATHEXPRESSION);
			if(v.size() > 1){
				throw new AttributeReferenceEvaluationException(context, ref, 
						"Found more than one value of=\"%s\"", ref.getContextSelectorId());
			}
			if(v.size() == 1){
				if(log.isDebugEnabled()){
					log.debug("Found ContextSelector attribute");
				}
				XPathExpressionValue xpath = (XPathExpressionValue)Iterables.getOnlyElement(v);
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
				contextNode = xpathProvider.evaluateToNode(context.getXPathVersion(), xpath.getValue(), content);
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

	class DefaultRequestAttributesCallback implements RequestContextAttributesCallback
	{
		@Override
		public BagOfAttributeValues getAttributeValues(
				AttributeCategories category, String attributeId, AttributeValueType dataType, String issuer) {
			Collection<Attributes> attributes = request.getAttributes(category);
			Attributes  found = Iterables.getOnlyElement(attributes);
			return dataType.bagType().create(found.getAttributeValues(attributeId, issuer, dataType));
		}

		@Override
		public BagOfAttributeValues getAttributeValues(
				AttributeCategories category, String attributeId, AttributeValueType dataType) {
			return getAttributeValues(category, attributeId, dataType, null);
		}

		@Override
		public <AV extends AttributeValue> AV getAttributeValue(
				AttributeCategories categoryId, String attributeId,
				AttributeValueType dataType, String issuer) {
			BagOfAttributeValues bag = getAttributeValues(categoryId, attributeId, dataType, issuer);
			return bag.isEmpty()?null:bag.<AV>value();
		}

		@Override
		public <AV extends AttributeValue> AV getAttributeValue(
				AttributeCategories categoryId, 
				String attributeId,
				AttributeValueType dataType) {
			BagOfAttributeValues bag = getAttributeValues(categoryId, attributeId, dataType);
			return bag.isEmpty()?null:bag.<AV>value();
		}	
	}
}
