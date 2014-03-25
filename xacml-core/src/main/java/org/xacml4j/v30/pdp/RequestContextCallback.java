package org.xacml4j.v30.pdp;

import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.Entity;

public interface RequestContextCallback
{
	/***
	 * Gets request context entity of the given category
	 * 
	 * @param category a request entity category
	 * @return {@link Entity}
	 */
	Entity getEntity(AttributeCategory category);
}
