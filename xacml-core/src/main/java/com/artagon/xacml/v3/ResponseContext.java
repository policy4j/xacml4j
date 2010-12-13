package com.artagon.xacml.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Preconditions;

public class ResponseContext extends XacmlObject
{
	private Collection<Result> results;
	
	public ResponseContext(Result result){
		this(Collections.singleton(result));
	}
	
	public ResponseContext(Collection<Result> results){
		Preconditions.checkNotNull(results);
		Preconditions.checkArgument(!results.isEmpty());
		this.results = new ArrayList<Result>(results);
	}
	
	public ResponseContext(Result ...results){
		this(Arrays.asList(results));
	}
	
	public Collection<Result> getResults(){
		return results;
	}
}
