package com.artagon.xacml.v3.types;

import com.artagon.xacml.v3.AttributeCategories;
import com.google.common.base.Preconditions;

public class XPathExpressionValue extends SimpleAttributeValue<String>
{
	private AttributeCategories categoryId;
	
	XPathExpressionValue(XPathExpressionType type, 
			String xpath, AttributeCategories categoryId){
		super(type, xpath);
		Preconditions.checkNotNull(categoryId);
		this.categoryId = categoryId;
	}
	
	public AttributeCategories getCategory(){
		return categoryId;
	}
}
