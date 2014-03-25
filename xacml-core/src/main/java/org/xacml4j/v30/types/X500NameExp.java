package org.xacml4j.v30.types;

import javax.security.auth.x500.X500Principal;

import org.xacml4j.v30.BagOfAttributeExp;

public final class X500NameExp extends BaseAttributeExp<X500Principal>
{
	private static final long serialVersionUID = -609417077475809404L;

	X500NameExp(X500Principal value) {
		super(XacmlTypes.X500NAME, value);
	}
	
	public static X500NameExp valueOf(String v){
		return new X500NameExp(new X500Principal(v));
	}
	
	public static X500NameExp valueOf(StringExp v){
		return valueOf(v.getValue());
	}
	
	public static X500NameExp valueOf(X500Principal p){
		return new X500NameExp(p);
	}
	
	public StringExp toStringExp(){
		return StringExp.valueOf(getValue().toString());
	}
	
	public static BagOfAttributeExp emptyBag(){
		return XacmlTypes.X500NAME.emptyBag();
	}
	
	public static BagOfAttributeExp.Builder bag(){
		return XacmlTypes.X500NAME.bag();
	}
}
