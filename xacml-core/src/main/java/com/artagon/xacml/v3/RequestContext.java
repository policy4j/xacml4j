package com.artagon.xacml.v3;

import java.util.LinkedList;
import java.util.List;

public class RequestContext 
{
	private boolean returnPolicyIdList;
	private List<Request> requests;
	
	public RequestContext(boolean returnPolicyIDList, 
			List<Request> requests)
	{
		this.returnPolicyIdList = returnPolicyIDList;
		this.requests = new LinkedList<Request>(requests);
	}
	
	public boolean isReturnPolicyIdList(){
		return returnPolicyIdList;
	}
	
	public Iterable<Request> getRequests(){
		return requests;
	}
}
