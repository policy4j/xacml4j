package org.xacml4j.v30.types;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.XPathExpression;

public final class XPathExp extends BaseAttributeExp<XPathExpression>
{
	private static final long serialVersionUID = 8576542145890616101L;

	XPathExp(XPathExpression xp){
		super(XacmlTypes.XPATH,
				xp);
	}
	
	public static XPathExp valueOf(XPathExpression xp){
		return new XPathExp(xp);
	}
	
	public static XPathExp valueOf(String xpath, CategoryId category){
		return new XPathExp(new XPathExpression(xpath, category));
	}

	public String getPath(){
		return getValue().getPath();
	}

	public CategoryId getCategory(){
		return getValue().getCategory();
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.XPATH.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.XPATH.bag();
	}
}
