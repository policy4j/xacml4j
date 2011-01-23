package com.artagon.xacml.v30;

public enum XPathVersion 
{
	XPATH1("http://www.w3.org/TR/1999/Rec-xpath-19991116"),
	XPATH2("http://www.w3.org/TR/2007/REC-xpath20-20070123");
	
	private String versionURI;
	
	private XPathVersion(String versionURI){
		this.versionURI = versionURI;
	}
	
	@Override
	public String toString(){
		return versionURI;
	}
	
	public static XPathVersion parse(String v)
	{
		for(XPathVersion version : XPathVersion.values()){
			if(version.versionURI.equalsIgnoreCase(v)){
				return version;
			}
		}
		return null;
	}
}
