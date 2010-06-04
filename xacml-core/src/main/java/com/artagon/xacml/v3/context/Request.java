package com.artagon.xacml.v3.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.AttributesReference;
import com.artagon.xacml.v3.policy.Condition;
import com.artagon.xacml.v3.policy.Target;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class Request extends XacmlObject
{	
	private boolean returnPolicyIdList;
	private Multimap<AttributeCategoryId, Attributes> attributes;
	private Map<String, Attributes> attributesByXmlId;
	private Collection<RequestReference> multipleRequests;
	
	/**
	 * Constructs a request with a given attributes
	 * @param attributes
	 */
	public Request(boolean returnPolicyIdList, 
			Collection<Attributes> attributes, 
			Collection<RequestReference> requestReferences)
	{
		this.returnPolicyIdList = returnPolicyIdList;
		this.attributes = HashMultimap.create();
		this.multipleRequests = new ArrayList<RequestReference>(requestReferences);
		this.attributesByXmlId = new HashMap<String, Attributes>();
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
	 * contained in this request
	 * 
	 * @return a map by category
	 */
	public Map<AttributeCategoryId, Collection<Attributes>> getAttributes(){
		return Multimaps.unmodifiableMultimap(attributes).asMap();
	}
	
	/**
	 * Gets all {@link Attributes} from request with
	 * a given category
	 * 
	 * @param categoryId an attribute category
	 * @return a collection of {@link Attributes} or
	 * {@link Collections#emptyList()} if given request
	 * does not have attributes of given category
	 */
	public Collection<Attributes> getAttributes(AttributeCategoryId categoryId){
		Preconditions.checkNotNull(categoryId);
		Collection<Attributes> attr =  attributes.get(categoryId);
		return (attr == null)?Collections.<Attributes>emptyList():attr;
	}
	
	public Collection<Node> getContent(AttributeCategoryId categoryId)
	{
		Preconditions.checkNotNull(categoryId);
		Collection<Attributes> byCategory =  attributes.get(categoryId);
		if(byCategory == null || 
				byCategory.isEmpty()){
			return Collections.<Node>emptyList();
		}
		Collection<Node> content = new ArrayList<Node>(byCategory.size());
		for(Attributes a : byCategory){
			content.add(a.getContent());
		}
		return content;
	}
	
	public boolean hasRepeatingCategories(){
		for(AttributeCategoryId category : getCategories()){
			if(getCategoryOccuriences(category) > 1){
				return false;
			}
		}
		return true;
	}
	
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
