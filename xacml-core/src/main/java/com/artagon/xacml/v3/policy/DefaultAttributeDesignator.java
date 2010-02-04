package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.AttributeCategoryId;

/**
 * The {@link DefaultAttributeDesignator} retrieves a bag of values for a 
 * named attribute from the request context. A named attribute is 
 * considered present if there is at least one attribute that 
 * matches the criteria set out below.
 * 
 * 
 * The {@Link AttributeDesignator} returns a bag containing all 
 * the attribute values that are matched by the named attribute. In the 
 * event that no matching attribute is present in the context, the 
 * {@link DefaultAttributeDesignator#isMustBePresent()} governs whether it
 * evaluates to an empty bag or {@link EvaluationIndeterminateException} exception. 
 * 
 * See XACML 3.0 core section 7.3.5.
 *  
 * @author Giedrius Trumpickas
 * 
 * @param <T>
 */
public final class DefaultAttributeDesignator extends BaseAttributeReference implements AttributeDesignator
{
	private String attributeId;
	private String issuer;
	private boolean mustBePresent;
	
	public DefaultAttributeDesignator(
			AttributeCategoryId  category,
			String attributeId, 
			String issuer,
			AttributeValueType dataType, 
			boolean mustBePresent){
		super(category, dataType);
		Preconditions.checkNotNull(attributeId);
		this.issuer = issuer;
		this.attributeId = attributeId;
		this.mustBePresent = mustBePresent;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AttributeDesignator#getAttributeId()
	 */
	public String getAttributeId(){
		return attributeId;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AttributeDesignator#getIssuer()
	 */
	public String getIssuer(){
		return issuer;
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.AttributeDesignator#isMustBePresent()
	 */
	public boolean isMustBePresent(){
		return mustBePresent;
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
	public BagOfAttributeValues<?> evaluate(EvaluationContext context)
			throws EvaluationException 
	{
		BagOfAttributeValues<?> bag = context.resolveAttributeDesignator(
				getCategory(), attributeId, evaluatesTo.getDataType(), issuer);
		if(bag.isEmpty() && isMustBePresent()){
			throw new EvaluationException(
					"Failed to resolve categoryId=\"%s\", attributeId=\"%s\", issuer=\"%s\"",
					getCategory(), getAttributeId(), getIssuer());
		}
		return bag;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
