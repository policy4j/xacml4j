package com.artagon.xacml.v30.spi.pip;

import java.util.Calendar;
import java.util.List;

import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.pdp.EvaluationException;

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
	 * @return a list {@link List} of {@link BagOfAttributeExp}
	 * instances
	 * @throws EvaluationException if an error
	 * occurs
	 */
	List<BagOfAttributeExp> getKeys();
}
