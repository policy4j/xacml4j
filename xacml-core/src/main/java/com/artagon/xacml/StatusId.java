package com.artagon.xacml;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum StatusId
{
	OK("urn:oasis:names:tc:xacml:1.0:status:ok"), 
	MISSING_ATTRIBUTE("urn:oasis:names:tc:xacml:1.0:status:missing-attribute"),
	SYNTAX_ERROR("urn:oasis:names:tc:xacml:1.0:status:syntax-error"),
	STATUS_PROCESSING_ERROR("urn:oasis:names:tc:xacml:1.0:status:processing-error");
	
	private String id;
	
	private static final Map<String, StatusId> BY_ID = new ConcurrentHashMap<String, StatusId>();

	static {
		for(StatusId t : EnumSet.allOf(StatusId.class)){
			BY_ID.put(t.id, t);
		}
	}
	
	private StatusId(String id){
		this.id = id;
	}
	
	public static StatusId parse(String v){
		return BY_ID.get(v);
	}
	
	@Override
	public String toString(){
		return id;
	}
}
