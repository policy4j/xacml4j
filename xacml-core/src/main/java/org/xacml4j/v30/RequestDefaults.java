package org.xacml4j.v30;

import com.google.common.base.Objects;



public class RequestDefaults
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

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("xpathVersion", xpathVersion)
				.toString();
	}

	@Override
	public int hashCode(){
		return xpathVersion.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof RequestDefaults)){
			return false;
		}
		RequestDefaults d = (RequestDefaults)o;
		return xpathVersion.equals(d.xpathVersion);
	}
}
