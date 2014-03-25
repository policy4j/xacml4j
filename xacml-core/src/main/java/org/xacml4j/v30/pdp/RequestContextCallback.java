package org.xacml4j.v30.pdp;

import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.Entity;


public interface RequestContextCallback
{
	Entity getEntity(AttributeCategory category);
}
