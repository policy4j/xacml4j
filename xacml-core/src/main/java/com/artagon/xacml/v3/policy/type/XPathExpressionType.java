package com.artagon.xacml.v3.policy.type;

import javax.xml.xpath.XPath;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeValueType;

public interface XPathExpressionType extends AttributeValueType
{
	/**
	 * Implementation throws {@link UnsupportedOperationException}
	 */
	XPathExpressionValue create(Object v);
	XPathExpressionValue create(XPath xpath, AttributeCategoryId category);
	
	/**
	 * Implementation throws {@link UnsupportedOperationException}
	 */
	XPathExpressionValue fromXacmlString(String v);
	
	public class XPathExpressionValue extends BaseAttributeValue<XPath>
	{
		private AttributeCategoryId categoryId;
		
		public XPathExpressionValue(XPathExpressionType type, 
				XPath xpath, AttributeCategoryId categoryId){
			super(type, xpath);
		}
		
		public AttributeCategoryId getAttributeCategory(){
			return categoryId;
		}
	}
}
