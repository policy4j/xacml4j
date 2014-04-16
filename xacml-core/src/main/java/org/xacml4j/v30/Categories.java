package org.xacml4j.v30;

import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public enum Categories implements CategoryId
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

	private CategoryId delegated;

	private static final String DELEGATED_CATEGORY_PREFIX= "urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:";

	private static final Map<String, CategoryId> BY_ID = new HashMap<String, CategoryId>();

	static
	{
		for(CategoryId category : EnumSet.allOf(Categories.class)){
			BY_ID.put(category.getId(), category);
			CategoryId delegate = category.toDelegatedCategory();
			if(delegate != null){
				BY_ID.put(delegate.getId(), delegate);
			}
		}
	}

	private Categories(
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
	public CategoryId toDelegatedCategory() {
		return delegated;
	}

	@Override
	public String toString(){
		return categoryURI;
	}

	/**
	 * Parses given value to the {@link Categories}
	 *
	 * @param v a value
	 * @return {@link Categories}
	 * @throws XacmlSyntaxException if given
	 * value can not be converted to the
	 * {@link Categories} value
	 */
	public static CategoryId parse(String v)
		throws XacmlSyntaxException
	{
		if(Strings.isNullOrEmpty(v)){
			throw new XacmlSyntaxException("Given value can't be " +
					"converted to XACML CategoryId");
		}
		CategoryId c = BY_ID.get(v);
		if(c == null){
			c = new CustomCategory(v);
		}
		return c;
	}
	
	public static CategoryId parse(URI categoryUri) throws XacmlSyntaxException{
		Preconditions.checkArgument(categoryUri != null);
		return parse(categoryUri.toString());
	}
	
	/**
	 * Tests if a given category URI represents
	 * a delegated category
	 *
	 * @param categoryURI a category URI
	 * @return {@code true} if a given category
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
		implements CategoryId
	{
		private String categoryURI;
		private CategoryId delegated;

		private CustomCategory(
				String categoryURI)
		{
			this.categoryURI = categoryURI;
			if(!Categories.isDelegate(categoryURI)){
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
		public CategoryId toDelegatedCategory() {
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
			if(!(obj instanceof CategoryId)){
				return false;
			}
			CategoryId c = (CategoryId)obj;
			return c.getId().equals(categoryURI);
		}

		@Override
		public String toString() {
			return categoryURI;
		}
	}
}
