package com.artagon.xacml;

import java.util.Arrays;
import java.util.Collection;

import com.artagon.xacml.util.Preconditions;

public class Response 
{
	private Collection<Result> result;
	
	public Response(Result ...results){
		Preconditions.checkArgument(results != null);
		Preconditions.checkArgument(results.length > 0);
		this.result = Arrays.asList(results);
	}
	
	public Collection<Result> getResults(){
		return result;
	}
}
