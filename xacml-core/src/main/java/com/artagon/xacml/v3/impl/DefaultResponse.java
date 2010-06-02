package com.artagon.xacml.v3.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

final class DefaultResponse extends XacmlObject implements Response
{
	private Collection<Result> results;
	
	public DefaultResponse(Collection<Result> results){
		Preconditions.checkNotNull(results);
		Preconditions.checkArgument(!results.isEmpty());
		this.results = new ArrayList<Result>(results);
	}
	
	public DefaultResponse(Result ...results){
		this(Arrays.asList(results));
	}
	
	@Override
	public Collection<Result> getResults(){
		return results;
	}
}
