package com.artagon.xacml.v3;

import com.artagon.xacml.util.Preconditions;

public final class PolicyIdentifier extends XacmlObject
{
	private String id;
	private Version version;
	
	public PolicyIdentifier(String id, Version version){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(version);
		this.id = id;
		this.version = version;
	}
	
	public String getId(){
		return id;
	}
	
	public Version getVersion(){
		return version;
	}
}
