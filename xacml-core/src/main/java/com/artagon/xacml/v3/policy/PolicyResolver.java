package com.artagon.xacml.v3.policy;


public interface PolicyResolver 
{
	Policy resolve(PolicyIDReference ref) 
		throws PolicyResolutionException;
	
	PolicySet resolve(PolicySetIDReference ref) 
		throws PolicyResolutionException;
}
