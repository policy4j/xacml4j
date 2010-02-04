package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Node;

public class Request 
{
	private boolean returnPolicyIdList = false;
	
	private Collection<Attributes> attributes;
	
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
	}
	
	public Collection<Attributes> getAttributes(){
		return Collections.unmodifiableCollection(attributes);
	}
	
	public Node getContent(AttributeCategoryId categoryId){
		return null;
	}
}
