package com.artagon.xacml.v3;

import org.w3c.dom.Node;

public interface RequestContextCallback 
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
			AttributeCategory category, 
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
			AttributeCategory category, 
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
	<AV extends AttributeValue> AV getAttributeValue(AttributeCategory categoryId, 
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
	<AV extends AttributeValue> AV getAttributeValue(AttributeCategory categoryId, 
			String attributeId, AttributeValueType dataType, String issuer);
	
	/**
	 * Gets {@link Attributes#getContent()} for a given category
	 * 
	 * @param category an attributes category
	 * @return {@link Node} or <code>null</code>
	 */
	Node getContent(AttributeCategory category);
}
