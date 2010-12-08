package com.artagon.xacml.v3.spi.repository;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;

public interface PolicyRepositoryEditor 
{
	/**
	 * Adds new policy set to this repository
	 * 
	 * @param ps a policy set
	 */
	void add(PolicySet ps);
	
	/**
	 * Adds new policy to this repository
	 * 
	 * @param p a policy
	 */
	void add(Policy p);
}
