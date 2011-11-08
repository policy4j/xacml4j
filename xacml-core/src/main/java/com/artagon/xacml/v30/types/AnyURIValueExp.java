package com.artagon.xacml.v30.types;

import java.net.URI;

public final class AnyURIValueExp extends BaseAttributeExpression<URI> 
{
	private static final long serialVersionUID = -1279561638068756670L;

	AnyURIValueExp(URI value){
		super(AnyURIType.ANYURI, value);
	}
	
	public AnyURIValueExp normalize(){
		return new AnyURIValueExp(getValue().normalize());
	}
}

