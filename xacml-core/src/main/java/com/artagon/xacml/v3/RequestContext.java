package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class RequestContext extends XacmlObject
{	
	private boolean returnPolicyIdList;
	private Multimap<AttributeCategoryId, Attributes> attributes;
	private Map<String, Attributes> byId;
	
	/**
	 * Constructs a request with a given attributes
	 * @param attributes
	 */
	public RequestContext(boolean returnPolicyIdList, 
			Collection<Attributes> attributes)
	{
		this.returnPolicyIdList = returnPolicyIdList;
		this.attributes = HashMultimap.create();
		this.byId = new HashMap<String, Attributes>();
		for(Attributes attr : attributes){
			this.attributes.put(attr.getCategoryId(), attr);
			if(attr.getId() != null){
				this.byId.put(attr.getId(), attr);
			}
		}
	}
	
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
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
		Collection<Attributes> attr =  attributes.get(categoryId);
		return (attr == null)?Collections.<Attributes>emptyList():attr;
	}
	
	/**
	 * Gets all {@link Attributes} instances
	 * from a given  request context which has
	 * attribute with a given identifier
	 * 
	 * @param categoryId an attribute category
	 * @param attributeId an attribute id
	 * @return a collection of {@link Attributes}
	 */
	public Collection<Attributes> getAttributes(
			final AttributeCategoryId categoryId, 
			final String attributeId)
	{
		 Collection<Attributes> attr = attributes.get(categoryId);
		 if(attr == null){
			 return Collections.emptyList();
		 }
		 return Collections2.filter(attr, new Predicate<Attributes>() {

			@Override
			public boolean apply(Attributes a) {
				return a.containsAttribute(attributeId);
			}
		});
	}
}
