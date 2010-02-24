package com.artagon.xacml.v3.policy;


public interface PolicyResolver 
{
	Policy resolve(PolicyIDReference ref);
	PolicySet resolve(PolicySetIDReference ref);
}
