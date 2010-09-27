package com.artagon.xacml.v3;

public interface RequestContextAttributesCallback 
{
	/**
	 * Gets {@link BagOfAttributeValues} from request context
	 * 
	 * @param <AV>
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @return {@link BagOfAttributeValues} or empty bag
	 * if no matching attribute exist in the request context
	 */
	BagOfAttributeValues getAttributeValues(
			AttributeCategories category, 
			String attributeId, 
			AttributeValueType dataType,
			String issuer);
	
	/**
	 * Method assumes that issuer is not specified for an
	 * attribute in the request context
	 * 
	 * @see {{@link #getAttribute(AttributeCategories, String, String)}
	 */
	BagOfAttributeValues getAttributeValues(
			AttributeCategories category, 
			String attributeId, 
			AttributeValueType dataType);
	
	/**
	 * Gets single attribute value from a request
	 * 
	 * @param <AV>
	 * @param categoryId an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute value data type
	 * @return {@link AV} instance or <code>null</code> if attribute
	 * with a given constraints can not be located in the request
	 */
	<AV extends AttributeValue> AV getAttributeValue(AttributeCategories categoryId, 
			String attributeId, AttributeValueType dataType);
	
	/**
	 * Gets single attribute value from a request
	 * 
	 * @param <AV>
	 * @param categoryId an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute value data type
	 * @param issuer an attribute issuer
	 * @return {@link AV} instance or <code>null</code> if attribute
	 * with a given constraints can not be located in the request
	 */
	<AV extends AttributeValue> AV getAttributeValue(AttributeCategories categoryId, 
			String attributeId, AttributeValueType dataType, String issuer);
}
