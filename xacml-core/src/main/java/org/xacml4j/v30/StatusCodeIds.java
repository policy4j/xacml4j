package org.xacml4j.v30;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public enum StatusCodeIds implements StatusCodeId
{
	OK("urn:oasis:names:tc:xacml:1.0:status:ok"), 
	MISSING_ATTRIBUTE("urn:oasis:names:tc:xacml:1.0:status:missing-attribute"),
	SYNTAX_ERROR("urn:oasis:names:tc:xacml:1.0:status:syntax-error"),
	STATUS_PROCESSING_ERROR("urn:oasis:names:tc:xacml:1.0:status:processing-error");
	
	private String id;
	
	private static final Map<String, StatusCodeIds> BY_ID = new ConcurrentHashMap<String, StatusCodeIds>();

	static {
		for(StatusCodeIds t : EnumSet.allOf(StatusCodeIds.class)){
			BY_ID.put(t.id, t);
		}
	}
	
	private StatusCodeIds(String id){
		this.id = id;
	}
	
	public static StatusCodeId parse(final String v){
		Preconditions.checkArgument(!Strings.isNullOrEmpty(v));
		StatusCodeId code = BY_ID.get(v);
		if(code != null){
			return code;
		}
		return new StatusCodeId() {
			@Override
			public String getId() {
				return v;
			}
			@Override
			public int hashCode(){
				return v.hashCode();
			}
			@Override
			public String toString(){
				return v;
			}
			@Override
			public boolean equals(Object o){
				if(o == this){
					return true;
				}
				if(o == null){
					return false;
				}
				if(!(o instanceof StatusCodeId)){
					return false;
				}
				StatusCodeId code = (StatusCodeId)o;
				return code.getId().equals(v);
			}
		};
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String toString(){
		return id;
	}
}
