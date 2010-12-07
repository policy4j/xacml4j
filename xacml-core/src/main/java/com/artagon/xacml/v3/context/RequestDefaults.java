package com.artagon.xacml.v3.context;

import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.XacmlObject;


public class RequestDefaults extends XacmlObject
{
	private XPathVersion xpathVersion;
	
	public RequestDefaults(XPathVersion xpathVersion){
		this.xpathVersion = xpathVersion;
	}
	
	public RequestDefaults(){
		this(XPathVersion.XPATH1);
	}
	
	public XPathVersion getXPathVersion(){
		return xpathVersion;
	}
}
