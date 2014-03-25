package org.xacml4j.v30.types;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.DNSName;

public final class DNSNameExp extends BaseAttributeExp<DNSName>
{
	private static final long serialVersionUID = -1729624624549215684L;

	DNSNameExp(DNSName value){
		super(XacmlTypes.DNSNAME, value);
	}
	
	public static DNSNameExp valueOf(String v){
		return new DNSNameExp(DNSName.parse(v));
	}
	
	public static DNSNameExp valueOf(StringExp v){
		return DNSNameExp.valueOf(v.getValue());
	}
	
	public static DNSNameExp valueOf(DNSName v){
		return new DNSNameExp(v);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toXacmlString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.DNSNAME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.DNSNAME.bag();
	}
}

