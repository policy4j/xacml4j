package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface Request 
{
	/**
	 * If the {@link #isReturnPolicyIdList()} returns 
	 * <code>true</code>, a PDP that implements this optional 
	 * feature MUST return a list of all policies which were 
	 * found to be fully applicable. That is, all policies 
	 * where both the {@link Target} matched and the {@link Condition} 
	 * evaluated to <code>true</code>, whether or not the {@link Effect}
	 *  was the same or different from the {@link Decision}}
	 *  
	 * @return boolean value
	 */
	boolean isReturnPolicyIdList();

	/**
	 * Gets request references contained
	 * in this request context
	 * 
	 * @return a collection of {@link RequestReference}
	 * instances
	 */
	Collection<RequestReference> getRequestReferences();

	/**
	 * Tests if this request has multiple
	 * individual XACML requests
	 * 
	 * @return <code>true</code> if this
	 * request has multiple XACML individual
	 * requests
	 */
	boolean hasMultipleRequests();

	/**
	 * Gets all attributes categories contained
	 * in this request context
	 * 
	 * @return an iterator over all categories
	 */
	Set<AttributeCategoryId> getCategories();
	
	int getCategoryOccuriences(AttributeCategoryId categoryId);

	/**
	 * Resolves attribute reference to {@link Attributes}
	 * 
	 * @param reference an attributes reference
	 * @return {@link Attributes} or <code>null</code> if
	 * reference can not be resolved
	 */
	Attributes getReferencedAttributes(AttributesReference reference);
	
	/**
	 * Gets all {@link Attributes} instances
	 * contained in this request
	 * 
	 * @return a map by category
	 */
	Map<AttributeCategoryId, Collection<Attributes>> getAttributes();
	
	/**
	 * Gets all {@link Attributes} from request with
	 * a given category
	 * 
	 * @param categoryId an attribute category
	 * @return a collection of {@link Attributes} or
	 * {@link Collections#emptyList()} if given request
	 * does not have attributes of given category
	 */
	Collection<Attributes> getAttributes(AttributeCategoryId categoryId);
	
	Collection<Attributes> getIncludeInResultAttributes();
	
	boolean hasRepeatingCategories();
}