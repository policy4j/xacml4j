package com.artagon.xacml;

public enum Categories implements CategoryId
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
	
	private final String id;
	
	Categories(String id){
		this.id = id;
	}
	
	public static CategoryId parse(final String v)
	{
		for(Categories category : values()){
			if(category.id.equals(v)){
				return category;
			}
		}
		return new CustomCategoryId(v);
	}
	
	@Override public String toString() {
		return id;
	}
}
