package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.google.common.base.Preconditions;

public class XPathExpressionValue extends SimpleAttributeValue<String>
{
	private AttributeCategoryId categoryId;
	
	XPathExpressionValue(XPathExpressionType type, 
			String xpath, AttributeCategoryId categoryId){
		super(type, xpath);
		Preconditions.checkNotNull(categoryId);
		this.categoryId = categoryId;
	}
	
	public AttributeCategoryId getCategory(){
		return categoryId;
	}
}
