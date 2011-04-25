package com.artagon.xacml.v30.marshall.jaxb;

import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.XacmlSyntaxException;

public interface XacmlParsingContext 
{	
	/**
	 * Resolves an {@link AttributeValueType}
	 * based on the given type identifier
	 * 
	 * @param typeId a type identifier
	 * @return {@link AttributeValueType} instance
	 * @throws XacmlSyntaxException if type
	 * can not be resolved to the valid XACML
	 * type
	 */
	AttributeValueType getType(String typeId) 
		throws XacmlSyntaxException;
}
