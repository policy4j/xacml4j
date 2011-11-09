package com.artagon.xacml.v30.pdp;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum StatusCodeId
{
	OK("urn:oasis:names:tc:xacml:1.0:status:ok"), 
	MISSING_ATTRIBUTE("urn:oasis:names:tc:xacml:1.0:status:missing-attribute"),
	SYNTAX_ERROR("urn:oasis:names:tc:xacml:1.0:status:syntax-error"),
	STATUS_PROCESSING_ERROR("urn:oasis:names:tc:xacml:1.0:status:processing-error");
	
	private String id;
	
	private static final Map<String, StatusCodeId> BY_ID = new ConcurrentHashMap<String, StatusCodeId>();

	static {
		for(StatusCodeId t : EnumSet.allOf(StatusCodeId.class)){
			BY_ID.put(t.id, t);
		}
	}
	
	private StatusCodeId(String id){
		this.id = id;
	}
	
	public static StatusCodeId parse(String v){
		return BY_ID.get(v);
	}
	
	@Override
	public String toString(){
		return id;
	}
}
