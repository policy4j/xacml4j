package com.artagon.xacml.v3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.google.common.base.Preconditions;

public class AttributeSelector extends 
	AttributeReference
{
	private final static Logger log = LoggerFactory.getLogger(AttributeSelector.class);
	
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
			AttributeValueType dataType, 
			boolean mustBePresent){
		this(category, xpath, null, dataType, mustBePresent);
	}
	
	public static AttributeSelector create(String categoryId, String xpath, 
			String contextAttributeId, String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException
	{
		AttributeCategoryId category = AttributeCategoryId.parse(categoryId);
		return create(category, xpath, contextAttributeId, dataTypeId, mustBePresent);
	}
	
	public static AttributeSelector create(
			AttributeCategoryId category, 
			String xpath, 
			String contextAttributeId, 
			String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException
	{
		AttributeValueType dataType = XacmlDataTypes.getType(dataTypeId);
		return new AttributeSelector(category, xpath, 
				contextAttributeId, dataType, mustBePresent);
	}
	
	public static AttributeSelector create(String categoryId, String xpath, 
			String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException
	{
		return create(categoryId, xpath, null, dataTypeId, mustBePresent);
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
	public BagOfAttributeValues evaluate(EvaluationContext context)
			throws EvaluationException 
	{ 
		BagOfAttributeValues v = null;
		try{
			v =  context.resolve(this);
		}catch(AttributeReferenceEvaluationException e){
			if(isMustBePresent()){
				throw e;
			}
			return getDataType().bagType().createEmpty();
		}catch(Exception e){
			if(isMustBePresent()){
				throw new AttributeReferenceEvaluationException(
						context, this, 
						StatusCode.createMissingAttribute(), e);
			}
			return getDataType().bagType().createEmpty();
		}
		if((v == null || 
				v.isEmpty()) 
				&& isMustBePresent()){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolved xpath=\"{}\", category=\"{}\"", 
						getPath(), getCategory());
			}
			throw new AttributeReferenceEvaluationException(context, this, 
				"Selector XPath expression=\"%s\" evaluated " +
				"to empty node set and mustBePresents=\"true\"", getPath());
		}
		return ((v == null)?getDataType().bagType().createEmpty():v);
	}
}
