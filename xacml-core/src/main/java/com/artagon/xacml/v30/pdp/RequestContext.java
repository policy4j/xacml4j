package com.artagon.xacml.v30.pdp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.StatusCode;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class RequestContext
{	
	private boolean returnPolicyIdList;
	private boolean combinedDecision;
	private Multimap<AttributeCategory, Attributes> attributes;
	private Map<String, Attributes> attributesByXmlId;
	private Collection<RequestReference> requestReferences;
	private RequestDefaults requestDefaults;
	
	private transient int cachedHashCode;
	
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
	public RequestContext(
			boolean returnPolicyIdList, 
			boolean combinedDecision,
			Collection<Attributes> attributes, 
			Collection<RequestReference> requestReferences, 
			RequestDefaults requestDefaults)
	{
		this.returnPolicyIdList = returnPolicyIdList;
		this.attributes = LinkedListMultimap.create();
		this.requestReferences = (requestReferences == null)?
				Collections.<RequestReference>emptyList():new ArrayList<RequestReference>(requestReferences);
		this.attributesByXmlId = new HashMap<String, Attributes>();
		this.requestDefaults = requestDefaults;
		this.combinedDecision = combinedDecision;
		if(attributes != null){
			for(Attributes attr : attributes){
				// index attributes by category
				this.attributes.put(attr.getCategory(), attr);
				// index attributes
				// by id for fast lookup
				if(attr.getId() != null){
					this.attributesByXmlId.put(attr.getId(), attr);
				}
			}
		}
		this.cachedHashCode = Objects.hashCode(
				this.returnPolicyIdList, 
				this.combinedDecision, 
				this.attributes, 
				this.requestReferences, 
				this.requestDefaults);
	}
	
	/**
	 * Constructs a request with a given attributes
	 * 
	 * @param attributes a collection of {@link Attributes}
	 * instances
	 */
	public RequestContext(boolean returnPolicyIdList, 
			boolean combinedDecision,
			Collection<Attributes> attributes, 
			Collection<RequestReference> requestReferences)
	{
		this(returnPolicyIdList, combinedDecision, attributes, 
				requestReferences, new RequestDefaults());
	}
	
	public RequestContext(boolean returnPolicyIdList, 
			boolean combinedDecision,
			Collection<Attributes> attributes)
	{
		this(returnPolicyIdList, combinedDecision, attributes, 
				Collections.<RequestReference>emptyList());
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
		this(returnPolicyIdList, false, attributes, 
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
		this(returnPolicyIdList, false, attributes, 
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
	 * Gets a flag used to request that the PDP combines multiple 
	 * decisions into a single decision. The use of this attribute 
	 * is specified in [Multi]. If the PDP does not implement the relevant 
	 * functionality in [Multi], then the PDP must return an Indeterminate 
	 * with a status code "Processing Error @{link {@link StatusCode#isProcessingError()} returns 
	 * <code>true</code> if it receives a request with this attribute 
	 * set to <code>true</code>
	 * 
	 * @return
	 */
	public boolean isCombinedDecision(){
		return combinedDecision;
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
	public boolean containsRepeatingCategories(){
		for(AttributeCategory category : getCategories()){
			if(getCategoryOccuriences(category) > 1){
				return true;
			}
		}
		return false;
	}
	
	public boolean containsAttributeValues(
			String attributeId, String issuer, AttributeExpType type)
	{
		for(Attributes a : getAttributes()){
			Collection<AttributeExp> values =  a.getAttributeValues(attributeId, issuer, type);
			if(!values.isEmpty()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tests if a given request context contains attributes with a given
	 * identifier of the given type for any category
	 * 
	 * @param attributeId an attribute identifier
	 * @param type an attribute type
	 * @return <code>true</code> if this request contains an at least
	 * one attribute with a given identifier and the given type
	 */
	public boolean containsAttributeValues(String attributeId, 
			AttributeExpType type)
	{
		return containsAttributeValues(attributeId, null, type);
	}
	
	/**
	 * Gets attribute values for given category, issuer, attribute id and data type
	 * 
	 * @param categoryId an category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @param issuer an attribute issuer
	 * @return a collection of {@link AttributeExp} instances
	 */
	public Collection<AttributeExp> getAttributeValues(AttributeCategory categoryId, 
			String attributeId, AttributeExpType dataType, String issuer)
	{
		Collection<AttributeExp> found = new LinkedList<AttributeExp>();
		for(Attributes a : attributes.get(categoryId)){
			found.addAll(a.getAttributeValues(attributeId, issuer, dataType));
		}
		return found;
	}
	
	/**
	 * Gets all attribute values of the given category with the
	 * given identifier and data type
	 * 
	 * @param categoryId an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @return a collection of {@link AttributeExp} instances
	 */
	public Collection<AttributeExp> getAttributeValues(
			AttributeCategory categoryId, 
			String attributeId, 
			AttributeExpType dataType)
	{
		return getAttributeValues(categoryId, attributeId, dataType, null);
	}
	
	/**
	 * Gets a single {@link AttributeExp} from this request
	 * 
	 * @param categoryId an attribute category identifier
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @return {@link AttributeExp} or <code>null</code>
	 */
	public AttributeExp getAttributeValue(AttributeCategory categoryId, 
			String attributeId, 
			AttributeExpType dataType){
		return Iterables.getOnlyElement(
				getAttributeValues(categoryId, attributeId, dataType), null);
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
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("ReturnPolicyIDList", returnPolicyIdList)
		.add("CombineDecision", combinedDecision)
		.addValue(attributes.values())
		.add("RequestReferences", requestReferences)
		.add("RequestDefaults", requestDefaults).toString();
	}
	
	@Override
	public int hashCode(){
		return cachedHashCode;
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof RequestContext)){
			return false;
		}
		RequestContext r = (RequestContext)o;
		return !(returnPolicyIdList ^ r.returnPolicyIdList) &&
			!(combinedDecision ^ r.combinedDecision) && attributes.equals(attributes) &&
			requestReferences.equals(r.requestReferences) && Objects.equal(requestDefaults, r.requestDefaults);
	}
}
