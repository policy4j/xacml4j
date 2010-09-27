package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeCategory;
import com.google.common.base.Preconditions;

public class XPathExpressionValue extends SimpleAttributeValue<String>
{
	private AttributeCategory categoryId;
	
	XPathExpressionValue(XPathExpressionType type, 
			String xpath, AttributeCategory categoryId){
		super(type, xpath);
		Preconditions.checkNotNull(categoryId);
		this.categoryId = categoryId;
	}
	
	public AttributeCategory getCategory(){
		return categoryId;
	}
}
