package com.artagon.xacml.v3;

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

import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.XPathEvaluationException;
import com.artagon.xacml.v3.spi.XPathProvider;
import com.artagon.xacml.v3.types.XPathExpressionType.XPathExpressionValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;
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
	private Map<AttributeDesignator, BagOfAttributeValues<? extends AttributeValue>> attributeDesignatorCache;
	
	/* Request scope attribute selector resolution cache */
	private Map<AttributeSelector, BagOfAttributeValues<AttributeValue>> attributeSelectorCache;
	
	/* Request scope attribute selector resolution cache */
	private Map<AttributeCategoryId, Node> contentCache;
	
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
		this.attributeDesignatorCache = new HashMap<AttributeDesignator, BagOfAttributeValues<? extends AttributeValue>>();
		this.attributeSelectorCache = new HashMap<AttributeSelector, BagOfAttributeValues<AttributeValue>>();
		this.contentCache = new HashMap<AttributeCategoryId, Node>();
	}
	
	
	@Override
	public Node getContent(EvaluationContext context,
			AttributeCategoryId categoryId) 
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
	public BagOfAttributeValues<? extends AttributeValue> resolve(EvaluationContext context, AttributeDesignator ref) 
		throws EvaluationException 
	{
		Collection<AttributeValue> values = request.getAttributeValues(ref.getCategory(), 
				ref.getAttributeId(), ref.getIssuer(), ref.getDataType());
		if(!values.isEmpty()){
			return ref.getDataType().bagOf().create(values);
		}
		if(context.getAttributeResolutionScope() == AttributeResolutionScope.REQUEST){
			return ref.getDataType().bagOf().createEmpty();
		}
		return doResolve(context, ref);
	}


	@Override
	public BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context, AttributeSelector ref)
			throws EvaluationException {
		
		BagOfAttributeValues<AttributeValue> v = attributeSelectorCache.get(ref);
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
	private final Node doGetContent(EvaluationContext context, AttributeCategoryId category) 
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
	private final BagOfAttributeValues<? extends AttributeValue> doResolve(
			EvaluationContext context,
			AttributeDesignator ref) throws EvaluationException 
	{
		BagOfAttributeValues<? extends AttributeValue> v = attributeDesignatorCache.get(ref);
		if(v == null){
			v = pip.resolve(context, ref, new DefaultRequestAttributesCallback());
			attributeDesignatorCache.put(ref, v);
		}
		return v;
	}
	
	@SuppressWarnings("unchecked")
	private final BagOfAttributeValues<AttributeValue> doResolve(
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
				return (BagOfAttributeValues<AttributeValue>) ref.getDataType().bagOf().createEmpty();
			}
			Node contextNode = content;
			Collection<AttributeValue> v = request.getAttributeValues(ref.getCategory(), 
						ref.getContextSelectorId() == null?CONTENT_SELECTOR:ref.getContextSelectorId(), 
								XacmlDataTypes.XPATHEXPRESSION.getDataType());
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
				return (BagOfAttributeValues<AttributeValue>) ref.getDataType().bagOf().createEmpty();
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
	
	@SuppressWarnings("unchecked")
	private BagOfAttributeValues<AttributeValue> toBag(EvaluationContext context,
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
	  	return (BagOfAttributeValues<AttributeValue>) ref.getDataType().bagOf().create(values);
	}

	class DefaultRequestAttributesCallback implements RequestContextAttributesCallback
	{

		@SuppressWarnings("unchecked")
		@Override
		public <AV extends AttributeValue> BagOfAttributeValues<AV> getAttributeValues(
				AttributeCategoryId category, String attributeId, AttributeValueType dataType, String issuer) {
			Collection<Attributes> attributes = request.getAttributes(category);
			Attributes  found = Iterables.getOnlyElement(attributes);
			return (BagOfAttributeValues<AV>)dataType.bagOf().create(found.getAttributeValues(attributeId, issuer, dataType));
		}

		@Override
		public <AV extends AttributeValue> BagOfAttributeValues<AV> getAttributeValues(
				AttributeCategoryId category, String attributeId, AttributeValueType dataType) {
			return getAttributeValues(category, attributeId, dataType, null);
		}

		@Override
		public <AV extends AttributeValue> AV getAttributeValue(
				AttributeCategoryId categoryId, String attributeId,
				AttributeValueType dataType, String issuer) {
			BagOfAttributeValues<AV> bag = getAttributeValues(categoryId, attributeId, dataType, issuer);
			return bag.isEmpty()?null:bag.value();
		}

		@Override
		public <AV extends AttributeValue> AV getAttributeValue(
				AttributeCategoryId categoryId, 
				String attributeId,
				AttributeValueType dataType) {
			BagOfAttributeValues<AV> bag = getAttributeValues(categoryId, attributeId, dataType);
			return bag.isEmpty()?null:bag.value();
		}	
	}
}
