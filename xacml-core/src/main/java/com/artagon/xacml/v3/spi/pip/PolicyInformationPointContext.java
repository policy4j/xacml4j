package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.RequestContextAttributesCallback;

public interface PolicyInformationPointContext 
	extends RequestContextAttributesCallback
{
	AttributeValue getCurrentTime();
	AttributeValue getCurrentDateTime();
	AttributeValue getCurrentDate();
	
	/**
	 * Gets value from this context via 
	 * given key
	 * @param key a key
	 * @return {@link Object} value for a given
	 * key or <code>null</code>
	 */
	Object getValue(Object key);	
	
	/**
	 * Set value to this context
	 * 
	 * @param key a key
	 * @param v a value
	 * @return {@link Object} an old value 
	 * or <code>null</code> if no old
	 * value exist in the context for a given key
	 */
	Object setValue(Object key, Object v);

}
