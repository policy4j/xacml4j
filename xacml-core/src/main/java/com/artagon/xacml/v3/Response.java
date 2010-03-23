package com.artagon.xacml.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.artagon.xacml.util.Preconditions;

public class Response extends XacmlObject
{
	private List<Result> results;
	
	public Response(Collection<Result> results){
		Preconditions.checkNotNull(results);
		this.results = new ArrayList<Result>(results);
	}
	
	public Response(Result ...results){
		this(Arrays.asList(results));
	}
	
	public Iterable<Result> getResults(){
		return results;
	}
}
