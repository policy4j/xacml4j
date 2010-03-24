package com.artagon.xacml.v3.profiles;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.RequestProcessingProfile;

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
