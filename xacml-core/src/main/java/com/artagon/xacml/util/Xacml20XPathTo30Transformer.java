package com.artagon.xacml.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.types.StringValue;
import com.artagon.xacml.v3.types.XPathExpressionType;
import com.artagon.xacml.v3.types.XPathExpressionValue;

public class Xacml20XPathTo30Transformer 
{
	private final static Logger log = LoggerFactory.getLogger(Xacml20XPathTo30Transformer.class);
	
	private final static String REQUEST_ELEMENT_NAME = "Request";
	private final static String RESOURCE_ELEMENT_NAME = "Resource";
	private final static String RESOURCE_CONTENT_ELEMENT_NAME = "ResourceContent";
	
	
	public static XPathExpressionValue fromXacml20String(StringValue path)
	{
		XPathExpressionValue xpathExp = XPathExpressionType.XPATHEXPRESSION.create(
				transform20PathTo30(path.getValue()), AttributeCategoryId.RESOURCE);
		return xpathExp;
	}
	
	public static String transform20PathTo30(String xpath)
	{
		StringBuffer buf = new StringBuffer(xpath);
		int firstIndex = xpath.indexOf(REQUEST_ELEMENT_NAME);
		if(firstIndex == -1){
			firstIndex = xpath.indexOf(RESOURCE_ELEMENT_NAME);
			if(firstIndex == -1){
				firstIndex = xpath.indexOf(RESOURCE_CONTENT_ELEMENT_NAME);
				if(firstIndex == -1){
					return xpath;
				}
			}
		}
		// found namespace prefix
		if(firstIndex > 0 && 
				buf.charAt(firstIndex - 1) == ':'){
			int index = xpath.indexOf("/");
			if(index == -1){
				firstIndex = 0;
			}
			else
			{
				firstIndex = index;
				while(xpath.charAt(index++) == '/'){
					firstIndex++;
				}
			}
		}
		int lastIndex = xpath.indexOf(RESOURCE_CONTENT_ELEMENT_NAME);
		if(lastIndex == -1){
			throw new IllegalArgumentException(
					String.format("Invalid XACML 2.0 xpath=\"%s\" " +
					"expression, \"ResourceContent\" is missing in the path", xpath));
		}
		lastIndex += RESOURCE_CONTENT_ELEMENT_NAME.length();
		buf.delete(firstIndex, lastIndex + 1);
		String transformedXpath =  buf.toString();
		if(log.isDebugEnabled()){
			log.debug("Original xpath=\"{}\", " +
					"transformed xpath=\"{}\"", xpath, transformedXpath);
		}
		return transformedXpath;
	}
}
