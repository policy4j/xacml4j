package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Request 
{
	private boolean returnPolicyIdList = false;
	
	private Map<AttributeCategoryId, Collection<Attributes>> attributesByCategory;
	
	public Request(boolean returnPolicyIDList, Iterable<Attributes> attributes){
		this.returnPolicyIdList = returnPolicyIDList;
		this.attributesByCategory = new HashMap<AttributeCategoryId, Collection<Attributes>>();
		for(Attributes attr : attributes){
			Collection<Attributes> byCategory = attributesByCategory.get(attr.getCategoryId());
			if(byCategory == null){
				byCategory = new LinkedList<Attributes>();
				attributesByCategory.put(attr.getCategoryId(), byCategory);
			}
			byCategory.add(attr);
		}
	}
	
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
	}
}
