package com.artagon.xacml.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.core.io.Resource;

public class ResourceCollection 
{
	private Collection<Resource> resources;
	
	public void setResources(Collection<Resource> resources){
		this.resources = new ArrayList<Resource>(resources);
	}
	
	public void setResource(Resource[] resources){
		this.resources = Arrays.asList(resources);
	}
	
	public Collection<Resource> getResources(){
		return resources;
	}
}
