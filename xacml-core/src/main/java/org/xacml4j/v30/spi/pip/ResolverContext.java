package org.xacml4j.v30.spi.pip;

import java.util.Calendar;
import java.util.List;

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationException;

import com.google.common.base.Ticker;


public interface ResolverContext 
{
	/**
	 * Gets current evaluation context date/time
	 * 
	 * @return {@link Calendar} instance
	 * representing current date/time
	 */
	Calendar getCurrentDateTime();
	
	Ticker getTicker();
	
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
