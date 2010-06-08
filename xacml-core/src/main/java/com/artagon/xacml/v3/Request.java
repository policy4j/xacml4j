package com.artagon.xacml.v3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.policy.AttributesReference;
import com.artagon.xacml.v3.policy.Condition;
import com.artagon.xacml.v3.policy.Target;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class Request extends XacmlObject
{	
	private boolean returnPolicyIdList;
	private Multimap<AttributeCategoryId, Attributes> attributes;
	private Map<String, Attributes> attributesByXmlId;
	private Collection<RequestReference> multipleRequests;
	private RequestDefaults requestDefaults;
	
	/**
	 * Constructs request with a given arguments
	 * 
	 * @param returnPolicyIdList a flag indicating
	 * that response should contains applicable 
	 * evaluated policy or policy set identifiers
	 * list
	 * @param attributes a collection of request attributes
	 * @param requestReferences a request references
	 * @param requestDefaults a request defaults
	 */
	public Request(boolean returnPolicyIdList, 
			Collection<Attributes> attributes, 
			Collection<RequestReference> requestReferences, 
			RequestDefaults requestDefaults)
	{
		Preconditions.checkNotNull(attributes);
		Preconditions.checkNotNull(requestReferences);
		Preconditions.checkNotNull(requestDefaults);
		this.returnPolicyIdList = returnPolicyIdList;
		this.attributes = HashMultimap.create();
		this.multipleRequests = new ArrayList<RequestReference>(requestReferences);
		this.attributesByXmlId = new HashMap<String, Attributes>();
		this.requestDefaults = requestDefaults;
		for(Attributes attr : attributes)
		{
			// index attributes by category
			this.attributes.put(attr.getCategoryId(), attr);
			// index attributes
			// by id for fast lookup
			if(attr.getId() != null){
				this.attributesByXmlId.put(attr.getId(), attr);
			}
		}
	}
	
	/**
	 * Constructs a request with a given attributes
	 * 
	 * @param attributes a collection of {@link Attributes}
	 * instances
	 */
	public Request(boolean returnPolicyIdList, 
			Collection<Attributes> attributes, 
			Collection<RequestReference> requestReferences)
	{
		this(returnPolicyIdList, attributes, 
				requestReferences, new RequestDefaults());
	}
	
	/**
	 * Constructs a request with a given attributes
	 * 
	 * @param attributes a collection of {@link Attributes}
	 * instances
	 */
	public Request(boolean returnPolicyIdList, 
			Collection<Attributes> attributes)
	{
		this(returnPolicyIdList, attributes, 
				Collections.<RequestReference>emptyList());
	}
	
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
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
	}
	
	/**
	 * Gets request defaults
	 * 
	 * @return an instance of {@link RequestDefaults}
	 */
	public RequestDefaults getRequestDefaults(){
		return requestDefaults;
	}
	
	/**
	 * Gets occurrence of the given category attributes 
	 * in this request
	 * 
	 * @param category a category
	 * @return a non-negative number indicating attributes of given 
	 * category occurrence in this request
	 */
	public int getCategoryOccuriences(AttributeCategoryId category){
		Collection<Attributes> attr = attributes.get(category);
		return (attr == null)?0:attr.size();
	}
	
	/**
	 * Gets request references contained
	 * in this request context
	 * 
	 * @return a collection of {@link RequestReference}
	 * instances
	 */
	public Collection<RequestReference> getRequestReferences(){
		return Collections.unmodifiableCollection(multipleRequests);
	}

	/**
	 * Tests if this request has multiple
	 * individual XACML requests
	 * 
	 * @return <code>true</code> if this
	 * request has multiple XACML individual
	 * requests
	 */
	public boolean hasMultipleRequests(){
		return !multipleRequests.isEmpty();
	}
	
	/**
	 * Gets all attributes categories contained
	 * in this request context
	 * 
	 * @return an iterator over all categories
	 */
	public Set<AttributeCategoryId> getCategories(){
		return Collections.unmodifiableSet(attributes.keySet());
	}
	
	/**
	 * Resolves attribute reference to {@link Attributes}
	 * 
	 * @param reference an attributes reference
	 * @return {@link Attributes} or <code>null</code> if
	 * reference can not be resolved
	 */
	public Attributes getReferencedAttributes(AttributesReference reference){
		Preconditions.checkNotNull(reference);
		return attributesByXmlId.get(reference.getReferenceId());
	}
	
	/**
	 * Gets all {@link Attributes} instances
	 * contained in this request by the category
	 * 
	 * @return a  map by category
	 */
	public Map<AttributeCategoryId, Collection<Attributes>> getAttributes(){
		Multimap<AttributeCategoryId, Attributes> attr = Multimaps.unmodifiableMultimap(attributes);
		return attr.asMap();
	}
	
	/**
	 * Gets all {@link Attributes} instances 
	 * from request of a given category
	 * 
	 * @param categoryId an attribute category
	 * @return a collection of {@link Attributes} or
	 * {@link Collections#emptyList()} if a given request
	 * does not have attributes of given category
	 */
	public Collection<Attributes> getAttributes(AttributeCategoryId categoryId){
		Preconditions.checkNotNull(categoryId);
		Collection<Attributes> attr =  attributes.get(categoryId);
		return (attr == null)?Collections.<Attributes>emptyList():attr;
	}
	
	/**
	 * Gets content as {@link Node} instance of 
	 * the given category
	 * 
	 * @param categoryId a category identifier
	 * @return {@link Node} or <code>null</code>
	 * if category does not have content or
	 * there is not attributes of given category
	 * in this request
	 * @exception IllegalArgumentException if request
	 * has more than one instance {@link Attributes}
	 * of given category
	 */
	public Node getContent(AttributeCategoryId categoryId) 
	{
		Preconditions.checkNotNull(categoryId);
		Collection<Attributes> byCategory =  attributes.get(categoryId);
		if(byCategory == null || 
				byCategory.isEmpty()){
			return null;
		}
		Preconditions.checkArgument(byCategory.size() == 1);
		return Iterables.getOnlyElement(byCategory).getContent();
	}
	
	/**
	 * Tests if this request has an multiple 
	 * {@link Attributes} instances with the
	 * same {@link Attributes#getCategoryId()} value
	 * 
	 * @return <code>true</code> if this request
	 * has multiple attributes of same category
	 */
	public boolean hasRepeatingCategories(){
		for(AttributeCategoryId category : getCategories()){
			if(getCategoryOccuriences(category) > 1){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets all {@link Attributes} instances
	 * containing an attributes with {@link Attribute#isIncludeInResult()}
	 * returning <code>true</code>
	 * 
	 * @return a collection of {@link Attributes} instances
	 * containing only {@link Attribute} with 
	 * {@link Attribute#isIncludeInResult()} returning <code>true</code>
	 */
	public Collection<Attributes> getIncludeInResultAttributes() 
	{
		Collection<Attributes> resultAttr = new LinkedList<Attributes>();
		for(Attributes a : attributes.values()){
			Collection<Attribute> includeInResult =  a.getIncludeInResultAttributes();
			if(!includeInResult.isEmpty()){
				resultAttr.add(new Attributes(a.getCategoryId(), includeInResult));
			}
		}
		return resultAttr;
	}		
}
