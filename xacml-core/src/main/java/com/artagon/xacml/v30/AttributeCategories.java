package com.artagon.xacml.v30;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AttributeCategories implements AttributeCategory
{ 
	
	ACTION("urn:oasis:names:tc:xacml:3.0:attribute-category:action"),
	ENVIRONMENT("urn:oasis:names:tc:xacml:3.0:attribute-category:environment"),
	RESOURCE("urn:oasis:names:tc:xacml:3.0:attribute-category:resource"),
	OBLIGATION("urn:oasis:names:tc:xacml:3.0:attribute-category:obligation"),
	STATUSDETAIL("urn:oasis:names:tc:xacml:3.0:attribute-category:status-detail"),
	SUBJECT_ACCESS("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"),
	SUBJECT_CODEBASE("urn:oasis:names:tc:xacml:1.0:subject-category:codebase"),
	SUBJECT_INTERMEDIARY("urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject"),
	SUBJECT_RECIPIENT("urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject"),
	SUBJECT_REQUESTING_MACHINE("urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine"),
	SUBJECT_ROLE_ENABLEMENT_AUTHORITY("urn:oasis:names:tc:xacml:2.0:subject-category:role-enablement-authority"),
	DELGATE("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate"),
	DELGATE_INFO("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate-info");
	
	
	private String categoryURI;
	
	private AttributeCategory delegated;
	
	private static final String DELEGATED_CATEGORY_PREFIX= "urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:";
	
	private static final Map<String, AttributeCategory> BY_ID = new HashMap<String, AttributeCategory>();
	
	static 
	{
		for(AttributeCategory category : EnumSet.allOf(AttributeCategories.class)){
			BY_ID.put(category.getId(), category);
			AttributeCategory delegate = category.toDelegatedCategory();
			if(delegate != null){
				BY_ID.put(delegate.getId(), delegate);
			}
		}
	}
	
	private AttributeCategories(
			String categoryURI){
		this.categoryURI = categoryURI;
		if(!isDelegate(categoryURI)){
			this.delegated = new CustomCategory(toDelegateURI(categoryURI));
		}
	}
	
	@Override
	public String getId(){
		return categoryURI;
	}
	
	@Override
	public boolean isDelegated() {
		return delegated != null;
	}
	
	
	@Override
	public AttributeCategory toDelegatedCategory() {
		return delegated;
	}

	@Override
	public String toString(){
		return categoryURI;
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
		if(c != null){
			return c;
		}
		return new CustomCategory(v);
	}
	
	/**
	 * Tests if a given category URI represents
	 * a delegated category
	 * 
	 * @param categoryURI a category URI
	 * @return <code>true</code> if a given category
	 * URI represents a delegated category
	 */
	private static boolean isDelegate(String categoryURI){
		return categoryURI.startsWith(DELEGATED_CATEGORY_PREFIX);
	}
	
	/**
	 * Creates a XACML 3.0 delegated category
	 * from a given category URI
	 * 
	 * @param categoryURI a category URI
	 * @return a delegated category URI
	 */
	private static String toDelegateURI(String categoryURI){
		if(categoryURI.startsWith(DELEGATED_CATEGORY_PREFIX)){
			return categoryURI;
		}
		return new StringBuilder(DELEGATED_CATEGORY_PREFIX.length() + categoryURI.length())
				.append(DELEGATED_CATEGORY_PREFIX)
				.append(categoryURI).toString();
	}

	private static class CustomCategory 
		implements AttributeCategory
	{
		private String categoryURI;
		private AttributeCategory delegated;
		
		private CustomCategory(
				String categoryURI)
		{
			this.categoryURI = categoryURI;
			if(!AttributeCategories.isDelegate(categoryURI)){
				this.delegated = new CustomCategory(toDelegateURI(categoryURI));
			}
		}
			
		@Override
		public String getId(){
			return categoryURI;
		}
		
		@Override
		public boolean isDelegated() {
			return delegated == null;
		}

		@Override
		public AttributeCategory toDelegatedCategory() {
			return delegated;
		}

		@Override
		public int hashCode() {
			return categoryURI.hashCode();
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
			return c.getId().equals(categoryURI);
		}
		
		@Override
		public String toString() {
			return categoryURI;
		}		
	}
}
