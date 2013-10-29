package org.xacml4j.v30.types;

import java.net.URI;

public final class AnyURIExp extends BaseAttributeExp<URI>
{
	private static final long serialVersionUID = -1279561638068756670L;

	AnyURIExp(URI value){
		super(AnyURIType.ANYURI, value);
	}

	public AnyURIExp normalize(){
		return new AnyURIExp(getValue().normalize());
	}
}

