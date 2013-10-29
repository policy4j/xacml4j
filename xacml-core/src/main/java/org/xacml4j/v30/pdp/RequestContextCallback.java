package org.xacml4j.v30.pdp;

import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeCategory;
import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;


public interface RequestContextCallback
{
	BagOfAttributeExp getAttributeValue(
			AttributeCategory category,
			String attributeId,
			AttributeExpType dataType,
			String issuer);

	Node getContent(AttributeCategory category);
}
