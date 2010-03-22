package com.artagon.xacml.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.artagon.xacml.util.Preconditions;

public class ResponseContext extends XacmlObject
{
	private List<Result> results;
	
	public ResponseContext(Collection<Result> results){
		Preconditions.checkNotNull(results);
		this.results = new ArrayList<Result>(results);
	}
	
	public ResponseContext(Result ...results){
		this(Arrays.asList(results));
	}
	
	public Iterable<Result> getResults(){
		return results;
	}
}
