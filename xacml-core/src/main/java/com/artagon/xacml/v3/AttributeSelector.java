package com.artagon.xacml.v3;


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
	String getPath();
	
	/**
	 * This attribute id refers to the attribute (by its AttributeId) 
	 * in the request context in the category given by the Category attribute.
	 * The referenced attribute MUST have data type 
	 * urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression, 
	 * and must select a single node in the content element.  
	 * The XPathCategory attribute of the referenced attribute MUST 
	 * be equal to the Category attribute of the attribute selector
	 * 
	 * @return
	 */
	String getContextSelectorId();
}