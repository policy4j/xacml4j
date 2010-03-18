package com.artagon.xacml.v3;

import java.util.Collection;

import com.artagon.xacml.v3.policy.AttributeValue;

public class Attribute <T extends AttributeValue>
{
	private String attributeId;
	private Collection<T> values;
	private boolean includeInResult;
	private String issuer;
	
	public String getAttributeId(){
		return attributeId;
	}
	
	public Iterable<T> getValues(){
		return values;
	}
	
	public String getIssuer(){
		return issuer;
	}
	
	public boolean isIncludeInResult(){
		return includeInResult;
	}
}
