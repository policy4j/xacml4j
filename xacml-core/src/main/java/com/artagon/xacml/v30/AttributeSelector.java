package com.artagon.xacml.v30;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.types.DataTypes;
import com.google.common.base.Objects;

public class AttributeSelector extends 
	AttributeReference
{
	private final static Logger log = LoggerFactory.getLogger(AttributeSelector.class);
	
	private AttributeSelectorKey selectorKey;
	
	public AttributeSelector(
			AttributeCategory category, 
			String xpath, 
			String contextAttributeId,
			AttributeValueType dataType, 
					boolean mustBePresent){
		super(mustBePresent);
		this.selectorKey = new AttributeSelectorKey(category, xpath, dataType, contextAttributeId);
	}
	
	public AttributeSelector(
			AttributeCategory category, 
			String xpath, 
			AttributeValueType dataType, 
			boolean mustBePresent){
		this(category, xpath, null, dataType, mustBePresent);
	}
	
	public static AttributeSelector create(String categoryId, String xpath, 
			String contextAttributeId, String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException
	{
		AttributeCategory category = AttributeCategories.parse(categoryId);
		return create(category, xpath, contextAttributeId, dataTypeId, mustBePresent);
	}
	
	public static AttributeSelector create(
			AttributeCategory category, 
			String xpath, 
			String contextAttributeId, 
			String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException
	{
		AttributeValueType dataType = DataTypes.getType(dataTypeId);
		return new AttributeSelector(category, xpath, 
				contextAttributeId, dataType, mustBePresent);
	}
	
	public static AttributeSelector create(String categoryId, String xpath, 
			String dataTypeId, boolean mustBePresent) 
		throws XacmlSyntaxException
	{
		return create(categoryId, xpath, null, dataTypeId, mustBePresent);
	}
	
	
	@Override
	public AttributeSelectorKey getReferenceKey() {
		return selectorKey;
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
		return selectorKey.getPath();
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
	public String getContextSelectorId(){
		return selectorKey.getContextSelectorId();
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("path", selectorKey.getPath())
		.add("category", selectorKey.getCategory())
		.add("contextSelectorId", selectorKey.getContextSelectorId())
		.add("mustBePresent", isMustBePresent())
		.toString();
	}
	
	@Override
	public void accept(ExpressionVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public BagOfAttributeValues evaluate(EvaluationContext context)
			throws EvaluationException 
	{ 
		BagOfAttributeValues v = null;
		try{
			v =  selectorKey.resolve(context);
		}catch(AttributeReferenceEvaluationException e){
			if(isMustBePresent()){
				throw e;
			}
			return getDataType().bagType().createEmpty();
		}catch(Exception e){
			if(isMustBePresent()){
				throw new AttributeReferenceEvaluationException(
						context, selectorKey, 
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
			throw new AttributeReferenceEvaluationException(
					context, selectorKey, 
				"Selector XPath expression=\"%s\" evaluated " +
				"to empty node set and mustBePresents=\"true\"", getPath());
		}
		return ((v == null)?getDataType().bagType().createEmpty():v);
	}
}
