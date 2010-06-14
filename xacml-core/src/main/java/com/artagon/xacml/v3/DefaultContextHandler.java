package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.artagon.xacml.v3.policy.spi.AttributesCallback;
import com.artagon.xacml.v3.policy.spi.XPathEvaluationException;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class DefaultContextHandler implements ContextHandler
{
	private final static Logger log = LoggerFactory.getLogger(DefaultContextHandler.class);
	
	private Request request;
	private XPathProvider xpathProvider;
	
	public DefaultContextHandler(XPathProvider xpathProvider, Request request)
	{
		Preconditions.checkNotNull(request);
		Preconditions.checkArgument(!request.hasRepeatingCategories());
		Preconditions.checkNotNull(xpathProvider);
		this.request = request;
		this.xpathProvider = xpathProvider;
	}
	
	@Override
	public final Node getContent(EvaluationContext context, AttributeCategoryId category) 
	{
		Attributes attr = request.getOnlyAttributes(category);
		if(attr == null){
			if(context.getAttributeResolutionScope() == 
				AttributeResolutionScope.REQUEST_EXTERNAL)
			return handleGetContent(category, request);
		}
		Node content = attr.getContent();
		if(content == null){
			if(context.getAttributeResolutionScope() == 
				AttributeResolutionScope.REQUEST_EXTERNAL){
				return handleGetContent(category, request);
			}
		}
		return content;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeDesignator ref) throws EvaluationException 
	{
		Attributes attributes = request.getOnlyAttributes(ref.getCategory());
		if(attributes != null){
			Collection<AttributeValue> values = attributes.getAttributeValues(
					ref.getAttributeId(), ref.getIssuer(), ref.getDataType());
			if(!values.isEmpty()){
				return (BagOfAttributeValues<AttributeValue>)ref.getDataType().bagOf().create(values);
			}
		} 
		return (context.getAttributeResolutionScope() == AttributeResolutionScope.REQUEST)? 
				((BagOfAttributeValues<AttributeValue>)ref.getDataType().bagOf().createEmpty()):handleResolve(ref, request);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeSelector ref) throws EvaluationException {
		try
		{
			Node content = getContent(context, ref.getCategory());
			if(content == null){
				return handleResolve(ref);
			}
			
			NodeList nodeSet = xpathProvider.evaluateToNodeSet(
					context.getXPathVersion(), ref.getPath(), content);
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
			throw new AttributeReferenceEvaluationException(context, ref, e);
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
							context, ref, "Unsupported DOM node type=\"%d\"", n.getNodeType());
			}
			try{
				values.add(ref.getDataType().fromXacmlString(v));
			}catch(Exception e){
				throw new AttributeReferenceEvaluationException(context, ref, 
						"Failed to convert xml node (at:%d in nodeset) " +
						"text value=\"%s\" to an attribute value of type=\"%s\"", 
						i, v, ref.getDataType());
			}
		}
	  	return (BagOfAttributeValues<AttributeValue>) ref.getDataType().bagOf().create(values);
	}
	
	/**
	 * A default implementation always returns <code>null</code>
	 */
	protected  Node handleGetContent(AttributeCategoryId category, Request request){
		return null;
	}
	
	/**
	 * A default implementation always returns an empty bag.
	 * 
	 * @param ref an attribute designator
	 * @param request a decision request
	 * @return an empty bag
	 */
	@SuppressWarnings("unchecked")
	protected BagOfAttributeValues<AttributeValue> handleResolve(AttributeDesignator ref, 
			Request request){
		return (BagOfAttributeValues<AttributeValue>) ref.getDataType().bagOf().createEmpty();
	}
	
	
	/**
	 * A default implementation always returns an empty bag.
	 * 
	 * @param ref an attribute selector
	 * @return an empty bag
	 */
	@SuppressWarnings("unchecked")
	protected BagOfAttributeValues<AttributeValue> handleResolve(AttributeSelector ref){
		return (BagOfAttributeValues<AttributeValue>) ref.getDataType().bagOf().createEmpty();
	}
	
	class DefaultRequestAttributesCallback implements AttributesCallback
	{

		@SuppressWarnings("unchecked")
		@Override
		public <AV extends AttributeValue> BagOfAttributeValues<AV> getAttribute(
				AttributeCategoryId category, String attributeId, AttributeValueType dataType, String issuer) {
			Collection<Attributes> attributes = request.getAttributes(category);
			Attributes  found = Iterables.getOnlyElement(attributes);
			return (BagOfAttributeValues<AV>)dataType.bagOf().create(found.getAttributeValues(attributeId, issuer, dataType));
		}

		@Override
		public <AV extends AttributeValue> BagOfAttributeValues<AV> getAttribute(
				AttributeCategoryId category, String attributeId, AttributeValueType dataType) {
			return getAttribute(category, attributeId, dataType, null);
		}
		
	}
}
