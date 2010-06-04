package com.artagon.xacml.v3.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

public class Response extends XacmlObject
{
	private Collection<Result> results;
	
	public Response(Collection<Result> results){
		Preconditions.checkNotNull(results);
		Preconditions.checkArgument(!results.isEmpty());
		this.results = new ArrayList<Result>(results);
	}
	
	public Response(Result ...results){
		this(Arrays.asList(results));
	}
	
	public Collection<Result> getResults(){
		return results;
	}
}
