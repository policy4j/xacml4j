package com.artagon.xacml.v3.spi.pip;

import java.util.Calendar;

import com.artagon.xacml.v3.RequestContextCallback;

public interface PolicyInformationPointContext 
{
	
	Calendar getCurrentDateTime();
	
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
	
	RequestContextCallback getRequestContextCallback();

}
