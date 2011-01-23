package com.artagon.xacml.v30.spi.pip;

import java.util.Calendar;
import java.util.List;

import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.EvaluationException;

public interface ResolverContext 
{
	/**
	 * Gets current evaluation context date/time
	 * 
	 * @return {@link Calendar} instance
	 * representing current date/time
	 */
	Calendar getCurrentDateTime();
	
	/**
	 * Gets resolver descriptor
	 * 
	 * @return {@link ResolverDescriptor}
	 */
	ResolverDescriptor getDescriptor();
	
	/**
	 * Gets request context keys
	 * 
	 * @return a list {@link List} of {@link BagOfAttributeValues}
	 * instances
	 * @throws EvaluationException if an error
	 * occurs
	 */
	List<BagOfAttributeValues> getKeys();
}
