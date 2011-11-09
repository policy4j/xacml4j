package com.artagon.xacml.v30.types;

import com.artagon.xacml.v30.core.AttributeCategory;
import com.artagon.xacml.v30.core.XPathExpression;

public final class XPathExp extends BaseAttributeExp<XPathExpression>
{
	private static final long serialVersionUID = 8576542145890616101L;
	
	XPathExp(XPathExpType type, 
			String xpath, AttributeCategory categoryId){
		super(type, new XPathExpression(xpath, categoryId));
	}
	
	public String getPath(){
		return getValue().getPath();
	}
	
	public AttributeCategory getCategory(){
		return getValue().getCategory();
	}

	@Override
	public String toXacmlString() {
		return getPath();
	}
}
