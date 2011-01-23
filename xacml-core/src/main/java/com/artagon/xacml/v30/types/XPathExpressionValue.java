package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.AttributeCategory;
import com.google.common.base.Preconditions;

public class XPathExpressionValue extends SimpleAttributeValue<String>
{
	private static final long serialVersionUID = 8576542145890616101L;
	
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
