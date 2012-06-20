package com.artagon.xacml.v30.pdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.StatusCode;
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
			AttributeExpType dataType,
					boolean mustBePresent){
		super(mustBePresent);
		this.selectorKey = new AttributeSelectorKey(category, xpath, dataType, contextAttributeId);
	}

	public AttributeSelector(
			AttributeCategory category,
			String xpath,
			AttributeExpType dataType,
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
		AttributeExpType dataType = DataTypes.getType(dataTypeId);
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
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeSelector)){
			return false;
		}
		AttributeSelector s = (AttributeSelector)o;
		return selectorKey.equals(s.selectorKey) &&
		(isMustBePresent() ^ s.isMustBePresent());
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(selectorKey, isMustBePresent());
	}

	@Override
	public void accept(ExpressionVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public BagOfAttributeExp evaluate(EvaluationContext context)
			throws EvaluationException
	{
		BagOfAttributeExp v = null;
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
						StatusCode.createMissingAttributeError(), e);
			}
			return getDataType().bagType().createEmpty();
		}
		if((v == null ||
				v.isEmpty())
				&& isMustBePresent()){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolve xpath=\"{}\", category=\"{}\"",
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
