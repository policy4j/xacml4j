package org.xacml4j.v30;

public interface CategoryId
{
	/**
	 * Gets XACML category identifier
	 *
	 * @return a XACML category identifier
	 */
	String getId();

	/**
	 * Tests if this category is delegated
	 *
	 * @return {@code true} if this
	 * category is delegated
	 */
	boolean isDelegated();

	/**
	 * Converts this category to
	 * XACML delegated category.
	 *
	 * @return {@link CategoryId}
	 */
	CategoryId toDelegatedCategory();
}
