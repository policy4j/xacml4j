package com.artagon.xacml.v3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class RequestContext extends XacmlObject
{	
	private boolean returnPolicyIdList;
	private Multimap<AttributeCategory, Attributes> attributes;
	private Map<String, Attributes> attributesByXmlId;
	private Collection<RequestReference> requestReferences;
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
	public RequestContext(boolean returnPolicyIdList, 
			Collection<Attributes> attributes, 
			Collection<RequestReference> requestReferences, 
			RequestDefaults requestDefaults)
	{
		Preconditions.checkNotNull(attributes);
		Preconditions.checkNotNull(requestReferences);
		Preconditions.checkNotNull(requestDefaults);
		this.returnPolicyIdList = returnPolicyIdList;
		this.attributes = LinkedListMultimap.create();
		this.requestReferences = new ArrayList<RequestReference>(requestReferences);
		this.attributesByXmlId = new HashMap<String, Attributes>();
		this.requestDefaults = requestDefaults;
		for(Attributes attr : attributes)
		{
			// index attributes by category
			this.attributes.put(attr.getCategory(), attr);
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
	public RequestContext(boolean returnPolicyIdList, 
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
	public RequestContext(boolean returnPolicyIdList, 
			Collection<Attributes> attributes)
	{
		this(returnPolicyIdList, attributes, 
				Collections.<RequestReference>emptyList());
	}
	
	/**
	 * Constructs a request with a given attributes
	 * 
	 * @param attributes a collection of {@link Attributes}
	 * instances
	 */
	public RequestContext(boolean returnPolicyIdList, 
			Collection<Attributes> attributes, 
			RequestDefaults requestDefaults)
	{
		this(returnPolicyIdList, attributes, 
				Collections.<RequestReference>emptyList(), requestDefaults);
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
	public int getCategoryOccuriences(AttributeCategory category){
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
		return Collections.unmodifiableCollection(requestReferences);
	}
	
	/**
	 * Gets all {@link Attributes} instances contained
	 * in this request
	 * @return a collection of {@link Attributes}
	 * instances
	 */
	public Collection<Attributes> getAttributes(){
		return Collections.unmodifiableCollection(attributes.values());
	}
	
	/**
	 * Tests if this request has multiple
	 * individual XACML requests
	 * 
	 * @return <code>true</code> if this
	 * request has multiple XACML individual
	 * requests
	 */
	public boolean containsRequestReferences(){
		return !requestReferences.isEmpty();
	}
	
	/**
	 * Gets all attribute categories contained
	 * in this request 
	 * 
	 * @return a set of all attribute categories in 
	 * this request
	 */
	public Set<AttributeCategory> getCategories(){
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
	 * from request of a given category
	 * 
	 * @param categoryId an attribute category
	 * @return a collection of {@link Attributes}, if
	 * a request does not have attributes of a specified
	 * category an empty collection is returned
	 */
	public Collection<Attributes> getAttributes(AttributeCategory categoryId){
		Preconditions.checkNotNull(categoryId);
		return Collections.unmodifiableCollection(attributes.get(categoryId));
	}
	
	/**
	 * Gets only one attribute of the given category
	 * 
	 * @param category a category identifier
	 * @return {@link Attributes} or <code>null</code>
	 * if request does not have attributes of given category
	 * @exception IllegalArgumentException if a request
	 * has more than one instance of {@link Attributes}
	 * of the requested category
	 */
	public Attributes getOnlyAttributes(AttributeCategory category){
		Collection<Attributes> attributes = getAttributes(category);
		return Iterables.getOnlyElement(attributes, null);
	}
	
	/**
	 * Gets content as {@link Node} instance of 
	 * the given category
	 * 
	 * @param categoryId a category identifier
	 * @return {@link Node} or <code>null</code>
	 * if category does not have content or
	 * there is no attributes of given category
	 * in this request
	 * @exception IllegalArgumentException if request
	 * has more than one instance of {@link Attributes}
	 * of the requested category
	 */
	public Node getOnlyContent(AttributeCategory categoryId) 
	{
		Attributes attributes = getOnlyAttributes(categoryId);
		return (attributes == null)?null:attributes.getContent();
	}
	
	/**
	 * Tests if this request has an multiple 
	 * {@link Attributes} instances with the
	 * same {@link Attributes#getCategory()} value
	 * 
	 * @return <code>true</code> if this request
	 * has multiple attributes of same category
	 */
	public boolean hasRepeatingCategories(){
		for(AttributeCategory category : getCategories()){
			if(getCategoryOccuriences(category) > 1){
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAttributeValues(String attributeId, 
			String issuer, AttributeValueType type)
	{
		for(Attributes a : getAttributes()){
			Collection<AttributeValue> values =  a.getAttributeValues(attributeId, issuer, type);
			if(!values.isEmpty()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets attribute values for given category, issuer, attribute id and data type
	 * 
	 * @param categoryId an category
	 * @param attributeId an attribute identifier
	 * @param issuer an attribute issuer
	 * @param dataType an attribute data type
	 * @return a collection of {@link AttributeValue} instances
	 */
	public Collection<AttributeValue> getAttributeValues(AttributeCategory categoryId, 
			String attributeId, String issuer, AttributeValueType dataType)
	{
		Collection<AttributeValue> found = new LinkedList<AttributeValue>();
		for(Attributes a : attributes.get(categoryId)){
			found.addAll(a.getAttributeValues(attributeId, issuer, dataType));
		}
		return found;
	}
	
	public Collection<AttributeValue> getAttributeValues(AttributeCategory categoryId, 
			String attributeId, AttributeValueType dataType)
	{
		return getAttributeValues(categoryId, attributeId, null, dataType);
	}
	
	public boolean containsAttributeValues(String attributeId, AttributeValueType type)
	{
		return containsAttributeValues(attributeId, null, type);
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
				resultAttr.add(new Attributes(a.getCategory(), includeInResult));
			}
		}
		return resultAttr;
	}		
}
