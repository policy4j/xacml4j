package com.artagon.xacml.v30;



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
