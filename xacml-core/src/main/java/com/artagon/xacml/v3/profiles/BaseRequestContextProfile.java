package com.artagon.xacml.v3.profiles;

import java.util.Collection;
import java.util.Iterator;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestProcessingProfile;
import com.artagon.xacml.v3.Result;

public abstract class BaseRequestContextProfile implements RequestProcessingProfile 
{
	private String id;
	
	/**
	 * Constructs request context profile with
	 * a given identifier
	 * 
	 * @param id a profile identifier
	 */
	protected BaseRequestContextProfile(String id){
		Preconditions.checkNotNull(id);
		this.id = id;
	}

	@Override
	public final String getId() {
		return id;
	}	
}
