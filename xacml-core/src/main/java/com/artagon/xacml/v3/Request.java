package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.artagon.xacml.v3.policy.Condition;
import com.artagon.xacml.v3.policy.Effect;
import com.artagon.xacml.v3.policy.Target;

public interface Request {

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
	public abstract boolean isReturnPolicyIdList();

	public abstract int getCategoryOccuriences(AttributeCategoryId category);

	public abstract Set<String> getProvidedAttributeIdentifiers();

	/**
	 * Gets request references contained
	 * in this request context
	 * 
	 * @return a collection of {@link RequestReference}
	 * instances
	 */
	public abstract Collection<RequestReference> getRequestReferences();

	/**
	 * Tests if this request has multiple
	 * individual XACML requests
	 * 
	 * @return <code>true</code> if this
	 * request has multiple XACML individual
	 * requests
	 */
	public abstract boolean hasMultipleRequests();

	/**
	 * Gets all attributes categories contained
	 * in this request context
	 * 
	 * @return an iterator over all categories
	 */
	public abstract Set<AttributeCategoryId> getCategories();

	/**
	 * Resolves attribute reference to {@link Attributes}
	 * 
	 * @param reference an attributes reference
	 * @return {@link Attributes} or <code>null</code> if
	 * reference can not be resolved
	 */
	public abstract Attributes getReferencedAttributes(
			AttributesReference reference);

	/**
	 * Gets all {@link Attributes} from request with
	 * a given category
	 * 
	 * @param categoryId an attribute category
	 * @return a collection of {@link Attributes} or
	 * {@link Collections#emptyList()} if given request
	 * does not have attributes of given category
	 */
	public abstract Collection<Attributes> getAttributes(
			AttributeCategoryId categoryId);

	public abstract Collection<Attributes> getAttributes();

	/**
	 * Gets all {@link Attributes} instances
	 * from a given  request context which has
	 * attribute with a given identifier
	 * 
	 * @param categoryId an attribute category
	 * @param attributeId an attribute id
	 * @return a collection of {@link Attributes}
	 */
	public abstract Collection<Attributes> getAttributes(
			final AttributeCategoryId categoryId, final String attributeId);

}