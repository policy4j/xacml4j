package com.artagon.xacml.v3;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Objects;

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
	
	ACTION_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:3.0:attribute-category:action", ACTION),
	ENVIRONMENT_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:3.0:attribute-category:environment", ENVIRONMENT),
	RESOURCE_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:3.0:attribute-category:resource", RESOURCE),
	OBLIGATION_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:3.0:attribute-category:obligation"),
	SUBJECT_ACCESS_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", SUBJECT_ACCESS),
	SUBJECT_CODEBASE_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:1.0:subject-category:codebase", SUBJECT_CODEBASE),
	SUBJECT_INTERMEDIARY_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject", SUBJECT_INTERMEDIARY),
	SUBJECT_RECIPIENT_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject", SUBJECT_RECIPIENT),
	SUBJECT_REQUESTING_MACHINE_DELEGATED("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine", SUBJECT_REQUESTING_MACHINE),
	DELGATE("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate"),
	DELGATE_INFO("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate-info");
	
	private String id;
	
	private AttributeCategory delegatedCategory;
	private static final String DELEGATED_CATEGORY_PREFIX= "urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:";
	
	private static final Map<String, AttributeCategories> BY_ID = new ConcurrentHashMap<String, AttributeCategories>();

	static {
		for(AttributeCategories t : EnumSet.allOf(AttributeCategories.class)){
			BY_ID.put(t.id, t);
		}
	}
	
	private AttributeCategories(String id, 
			AttributeCategory delegated){
		this.id = id;
		this.delegatedCategory = delegated;
	}
	
	private AttributeCategories(String id){
		this(id, null);
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public boolean isDelegated() {
		return delegatedCategory != null;
	}

	@Override
	public AttributeCategory getDelegatedCategory() {
		return delegatedCategory;
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
			AttributeCategory delegated = null;
			if(v.startsWith(DELEGATED_CATEGORY_PREFIX)){
				String delegatedCategory = v.substring(DELEGATED_CATEGORY_PREFIX.length());
				delegated = BY_ID.get(delegatedCategory);
				if(delegated == null){
					delegated = new InternalAttributeCategoryImpl(delegatedCategory);
				}
			}
			c = new InternalAttributeCategoryImpl(v, delegated);
		}
		return c;
	}

	
	private static class InternalAttributeCategoryImpl 
		implements AttributeCategory
	{
		private String categoryId;
		private AttributeCategory delegatedCategory;
		
		private InternalAttributeCategoryImpl(String categoryId, 
				AttributeCategory delegatedCategory){
			this.categoryId = categoryId;
			this.delegatedCategory = delegatedCategory;
		}
		
		private InternalAttributeCategoryImpl(String categoryId){
			this(categoryId, null);
		}
		
		public boolean isDelegated(){
			return (delegatedCategory != null);
		}
		
		public AttributeCategory getDelegatedCategory(){
			return delegatedCategory;
		}
		
		@Override
		public String getId(){
			return categoryId;
		}
		
		@Override
		public int hashCode() {
			return 31 * categoryId.hashCode() + 
			((delegatedCategory != null)
					?delegatedCategory.hashCode():0);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj){
				return true;
			}
			if(!(obj instanceof InternalAttributeCategoryImpl)){
				return false;
			}
			InternalAttributeCategoryImpl c = (InternalAttributeCategoryImpl)obj;
			return c.getId().equals(categoryId) && 
			Objects.equal(delegatedCategory, c.delegatedCategory);
		}
		
		@Override
		public String toString() {
			return categoryId;
		}		
	}
}
