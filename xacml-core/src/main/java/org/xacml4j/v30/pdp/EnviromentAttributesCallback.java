package org.xacml4j.v30.pdp;

import java.util.Map;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;

public interface EnviromentAttributesCallback 
{
	Map<AttributeDesignatorKey, BagOfAttributeExp> getAttributes();
}
