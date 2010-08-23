package com.artagon.xacml.spring;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.core.io.Resource;

public class ResourceCollection 
{
	private Collection<Resource> resources = new LinkedList<Resource>();
	
	public void setResources(Collection<Resource> resources){
		this.resources = resources;
	}
	
	public Collection<Resource> getResources(){
		return resources;
	}
}
