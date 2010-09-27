package com.artagon.xacml.v3;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum AttributeCategories implements AttributeCategory
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
	
	private static final Map<String, AttributeCategories> BY_ID = new ConcurrentHashMap<String, AttributeCategories>();

	static {
		for(AttributeCategories t : EnumSet.allOf(AttributeCategories.class)){
			BY_ID.put(t.id, t);
		}
	}
	
	private AttributeCategories(String id){
		this.id = id;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public String toString(){
		return id;
	}
	
	/**
	 * Parses given value to the {@link AttributeCategories}
	 * 
	 * @param v a value
	 * @return {@link AttributeCategories}
	 * @throws XacmlSyntaxException if given
	 * value can not be converted to the
	 * {@link AttributeCategories} value
	 */
	public static AttributeCategory parse(String v) 
		throws XacmlSyntaxException
	{
		if(v == null){
			return null;
		}
		AttributeCategory c = BY_ID.get(v);
		if(c == null){
			c = new CustomAttributeCategory(v);
		}
		return c;
	}

	
	private static class CustomAttributeCategory 
		implements AttributeCategory{
		private String categoryId;
		
		private CustomAttributeCategory(String categoryId){
			this.categoryId = categoryId;
		}
		
		@Override
		public String getId(){
			return categoryId;
		}
		
		@Override
		public int hashCode() {
			return categoryId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj){
				return true;
			}
			if(!(obj instanceof AttributeCategory)){
				return false;
			}
			AttributeCategory c = (AttributeCategory)obj;
			return c.getId().equals(categoryId);
		}
		
		@Override
		public String toString() {
			return categoryId;
		}		
	}
}
