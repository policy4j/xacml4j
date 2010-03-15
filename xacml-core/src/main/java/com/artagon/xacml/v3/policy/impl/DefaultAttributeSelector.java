package com.artagon.xacml.v3.policy.impl;

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

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.StatusCode;
import com.artagon.xacml.v3.policy.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.policy.AttributeSelector;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.artagon.xacml.v3.policy.XPathEvaluationException;


final class DefaultAttributeSelector extends 
	BaseAttributeReference implements AttributeSelector
{
	private final static Logger log = LoggerFactory.getLogger(DefaultAttributeSelector.class);
	
	private String xpath;
	private String contextAttributeId;
	
	public DefaultAttributeSelector(
			AttributeCategoryId category, 
			String xpath, 
			AttributeValueType dataType, 
					boolean mustBePresent){
		super(category, dataType, mustBePresent);
		Preconditions.checkNotNull(xpath);
		this.xpath = xpath;
	}
	
	@Override
	public String getSelect(){
		return xpath;
	}
	
	@Override
	public String getContextAttributeId()
	{
		return contextAttributeId;
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public BagOfAttributeValues<?> evaluate(EvaluationContext context)
			throws EvaluationException 
	{ 
		Node node = context.getContent(getCategory());
		if(node == null){
			if(isMustBePresent()){
				throw new AttributeReferenceEvaluationException(context, this, 
					"Content node for category=\"%s\" is null and mustBePresent=true", 
					getCategory());
			}
			return getDataType().bagOf().createEmpty();
		}
		try
		{
			NodeList nodeSet = selectNodes(context, node);
			if(nodeSet == null || 
					nodeSet.getLength() == 0){
				log.debug("Selected nodeset via xpath=\"{}\" and category=\"{}\" is empty", 
						xpath, getCategory());
				if(isMustBePresent()){
					throw new AttributeReferenceEvaluationException(context, this, 
						"Selector XPath expression=\"%s\" evaluated " +
						"to empty node set and mustBePresents=\"true\"", xpath);
				}
			}
			if(log.isDebugEnabled()){
				log.debug("Found=\"{}\" nodes via xpath=\"{}\" and category=\"{}\"", 
						new Object[]{nodeSet.getLength(), xpath, getCategory()});
			}
			return toBag(context, nodeSet);
		}
		catch(XPathEvaluationException e){
			throw new AttributeReferenceEvaluationException(context, this, e);
		}
	}
	
	private NodeList selectNodes(EvaluationContext context, Node contextNode) 
		throws XPathEvaluationException
	{
		NodeList nodeSet = context.evaluateToNodeSet(xpath, contextNode);
		return nodeSet;
	}
	
	private BagOfAttributeValues<?> toBag(EvaluationContext context, NodeList nodeSet) 
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
							context, this, "Unsupported DOM node type=\"%d\"", n.getNodeType());
			}
			try{
				values.add(getDataType().fromXacmlString(v));
			}catch(Exception e){
				throw new AttributeReferenceEvaluationException(context, this, 
						"Failed to convert xml node (at:%d in nodeset) " +
						"text value=\"%s\" to an attribute value of type=\"%s\"", 
						i, v, getDataType());
			}
		}
	  	return getDataType().bagOf().create(values);
	}
}
