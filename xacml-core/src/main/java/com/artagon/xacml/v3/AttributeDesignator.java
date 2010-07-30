package com.artagon.xacml.v3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The {@link AttributeDesignator} retrieves a bag of values for a 
 * named attribute from the request context. A named attribute is 
 * considered present if there is at least one attribute that 
 * matches the criteria set out below.
 * 
 * 
 * The {@Link AttributeDesignator} returns a bag containing all 
 * the attribute values that are matched by the named attribute. In the 
 * event that no matching attribute is present in the context, the 
 * {@link AttributeDesignator#isMustBePresent()} governs whether it
 * evaluates to an empty bag or {@link EvaluationIndeterminateException} exception. 
 * 
 * See XACML 3.0 core section 7.3.5.
 *  
 * @author Giedrius Trumpickas
 * 
 * @param <T>
 */
public class AttributeDesignator extends AttributeReference 
{
	private final static Logger log = LoggerFactory.getLogger(AttributeDesignator.class);
	
	private String attributeId;
	private String issuer;
	
	AttributeDesignator(
			AttributeCategoryId  category,
			String attributeId, 
			String issuer,
			AttributeValueType dataType, 
			boolean mustBePresent){
		super(category, dataType, mustBePresent);
		Preconditions.checkNotNull(attributeId);
		this.issuer = issuer;
		this.attributeId = attributeId;
	}
	
	/**
	 * Gets attribute identifier
	 * in the request context
	 * 
	 * @return attribute identifier
	 * in the request context
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	public String getIssuer(){
		return issuer;
	}
		
	/**
	 * Evaluates this attribute designator by resolving
	 * attribute via {@link EvaluationContext#resolveAttributeDesignator(String, 
	 * String, AttributeValueType, String)
	 * 
	 * @return {@link BagOfAttributeValues} instance 
	 * @exception EvaluationIndeterminateException if attribute can't be resolved
	 * and {@link this#mustBePresent} is true
	 */
	@SuppressWarnings("unchecked")
	public BagOfAttributeValues<AttributeValue> evaluate(EvaluationContext context)
			throws EvaluationException 
	{
		BagOfAttributeValues<AttributeValue> bag = context.resolve(this);
		if((bag == null || bag.isEmpty()) && 
				isMustBePresent()){
			if(log.isDebugEnabled()){
				log.debug("Failed to resolved attributeId=\"{}\", category=\"{}\"", 
						getAttributeId(), getCategory());
			}
			throw new AttributeReferenceEvaluationException(context, this,
					"Failed to resolve categoryId=\"%s\", attributeId=\"%s\", issuer=\"%s\"",
					getCategory(), getAttributeId(), getIssuer());
		}
		return (BagOfAttributeValues<AttributeValue>)((bag == null)?getDataType().bagOf().createEmpty():bag);
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
