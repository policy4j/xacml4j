package com.artagon.xacml.policy;

import com.artagon.xacml.CategoryId;
import com.artagon.xacml.util.Preconditions;

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
public final class AttributeDesignator extends AttributeReference
{
	private String attributeId;
	private String issuer;
	private boolean mustBePresent;
	
	public AttributeDesignator(
			CategoryId  category,
			String attributeId, 
			String issuer,
			AttributeType dataType, 
			boolean mustBePresent){
		super(category, dataType);
		Preconditions.checkNotNull(attributeId);
		this.issuer = issuer;
		this.attributeId = attributeId;
		this.mustBePresent = mustBePresent;
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
	
	/**
	 * Gets attribute issuer.
	 * 
	 * @return attribute issuer
	 * or <code>null</code> if issuer
	 * is not specified
	 */
	public String getIssuer(){
		return issuer;
	}
	
	/**
	 * Governs whether this designator evaluates 
	 * to an empty bag or {@link EvaluationIndeterminateException} 
	 * exception is thrown
	 * 
	 * @return
	 */
	public boolean isMustBePresent(){
		return mustBePresent;
	}
	
	/**
	 * Evaluates this attribute designator by resolving
	 * attribute via {@link EvaluationContext#resolveAttributeDesignator(String, 
	 * String, AttributeType, String)
	 * 
	 * @return {@link BagOfAttributes} instance 
	 * @exception EvaluationIndeterminateException if attribute can't be resolved
	 * and {@link this#mustBePresent} is true
	 */
	public BagOfAttributes<?> evaluate(EvaluationContext context)
			throws EvaluationException 
	{
		BagOfAttributes<?> bag = context.resolveAttributeDesignator(
				getCategory(), attributeId, evaluatesTo.getDataType(), issuer);
		if(bag.isEmpty() && isMustBePresent()){
			throw new EvaluationIndeterminateException(
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
