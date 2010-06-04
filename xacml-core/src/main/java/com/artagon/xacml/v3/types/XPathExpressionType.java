package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValueType;
import com.google.common.base.Preconditions;

public interface XPathExpressionType extends AttributeValueType
{
	XPathExpressionValue create(Object v, Object ...params);
	XPathExpressionValue create(String xpath, AttributeCategoryId category);
	XPathExpressionValue fromXacmlString(String v, Object ...params);
	
	public class XPathExpressionValue extends BaseAttributeValue<String>
	{
		private AttributeCategoryId categoryId;
		
		public XPathExpressionValue(XPathExpressionType type, 
				String xpath, AttributeCategoryId categoryId){
			super(type, xpath);
			Preconditions.checkNotNull(categoryId);
			this.categoryId = categoryId;
		}
		
		public AttributeCategoryId getAttributeCategory(){
			return categoryId;
		}
	}
}
