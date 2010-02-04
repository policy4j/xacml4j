package com.artagon.xacml.v3.policy.type;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeValueType;

public interface XPathExpressionType extends AttributeValueType
{
	/**
	 * Implementation throws {@link UnsupportedOperationException}
	 */
	XPathExpressionValue create(Object v);
	XPathExpressionValue create(String xpath, AttributeCategoryId category) throws XPathExpressionException;
	
	/**
	 * Implementation throws {@link UnsupportedOperationException}
	 */
	XPathExpressionValue fromXacmlString(String v);
	
	public class XPathExpressionValue extends BaseAttributeValue<XPathExpression>
	{
		private AttributeCategoryId categoryId;
		
		public XPathExpressionValue(XPathExpressionType type, 
				XPathExpression xpath, AttributeCategoryId categoryId){
			super(type, xpath);
		}
		
		public AttributeCategoryId getAttributeCategory(){
			return categoryId;
		}
	}
}
