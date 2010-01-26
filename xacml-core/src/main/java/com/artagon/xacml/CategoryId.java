package com.artagon.xacml;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum CategoryId
{ 
	ACTION("urn:oasis:names:tc:xacml:3.0:attribute-category:action"),
	ENVIRONMENT("urn:oasis:names:tc:xacml:3.0:attribute-category:environment"),
	OBLIGATION("urn:oasis:names:tc:xacml:3.0:attribute-category:obligation"),
	RESOURCE("urn:oasis:names:tc:xacml:3.0:attribute-category:resource"),
	STATUSDETAIL("urn:oasis:names:tc:xacml:3.0:attribute-category:status-detail"),
	SUBJECT_ACCESS("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"),
	SUBJECT_CODEBASE("urn:oasis:names:tc:xacml:1.0:subject-category:codebase"),
	SUBJECT_INTERMEDIARY("urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject"),
	SUBJECT_RECIPIENT("urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject"),
	SUBJECT_REQUESTING_MACHINE("urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine");
	
	
	private String id;
	
	private static final Map<String, CategoryId> BY_ID = new ConcurrentHashMap<String, CategoryId>();

	static {
		for(CategoryId t : EnumSet.allOf(CategoryId.class)){
			BY_ID.put(t.id, t);
		}
	}
	
	private CategoryId(String id){
		this.id = id;
	}
	
	public static CategoryId parse(String v){
		return BY_ID.get(v);
	}
	
	@Override
	public String toString(){
		return id;
	}
}
