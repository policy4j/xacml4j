package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;


public class AttributeSelector extends 
	BaseAttributeReference
{
	private String xpath;
	private String contextAttributeId;
	
	public AttributeSelector(
			AttributeCategoryId category, 
			String xpath, 
			String contextAttributeId,
			AttributeValueType dataType, 
					boolean mustBePresent){
		super(category, dataType, mustBePresent);
		Preconditions.checkNotNull(xpath);
		this.xpath = xpath;
		this.contextAttributeId = contextAttributeId;
	}
	
	public AttributeSelector(
			AttributeCategoryId category, 
			String xpath, 
			AttributeValueType dataType, boolean mustBePresent){
		this(category, xpath, null, dataType, mustBePresent);
	}
	
	/**
	 * An XPath expression whose context node is the Content 
	 * element of the attribute category indicated by the Category 
	 * attribute. There SHALL be no restriction on the XPath syntax, 
	 * but the XPath MUST NOT refer to or traverse any content 
	 * outside the Content element in any way.
	 * 
	 * @return an XPath expression
	 */
	public String getPath(){
		return xpath;
	}
	
	/**
	 * This attribute id refers to the attribute (by its AttributeId) 
	 * in the request context in the category given by the Category attribute.
	 * The referenced attribute MUST have data type 
	 * urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression, 
	 * and must select a single node in the content element.  
	 * The XPathCategory attribute of the referenced attribute MUST 
	 * be equal to the Category attribute of the attribute selector
	 * 
	 * @return
	 */
	public String getContextSelectorId()
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
		BagOfAttributeValues<AttributeValue> bag =  context.resolve(this);
		if((bag == null || 
				bag.isEmpty()) 
				&& isMustBePresent()){
			throw new AttributeReferenceEvaluationException(context, this, 
				"Selector XPath expression=\"%s\" evaluated " +
				"to empty node set and mustBePresents=\"true\"", getPath());
		}
		return (bag == null)?getDataType().bagOf().createEmpty():bag;
	}
	
	
}
