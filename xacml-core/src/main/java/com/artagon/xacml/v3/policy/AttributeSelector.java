package com.artagon.xacml.v3.policy;


public interface AttributeSelector extends AttributeReference
{
	/**
	 * An XPath expression whose context node is the Content 
	 * element of the attribute category indicated by the Category 
	 * attribute. There SHALL be no restriction on the XPath syntax, 
	 * but the XPath MUST NOT refer to or traverse any content 
	 * outside the Content element in any way.
	 * 
	 * @return an XPath expression
	 */
	String getSelect();
	
	String getContextAttributeId();
}