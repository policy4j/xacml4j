package com.artagon.xacml.v30;

import java.util.Map;

public class Request 
{
	private boolean returnPolicyIdList;
	
	private Map<CategoryId, Attributes> attributes;
	
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
	}
	
	/**
	 * Gets attributes via given {@link CategoryId}
	 * 
	 * @param categoryId a cate
	 * @return
	 */
	public Attributes getAttributes(CategoryId categoryId){
		return attributes.get(categoryId);
	}
}
