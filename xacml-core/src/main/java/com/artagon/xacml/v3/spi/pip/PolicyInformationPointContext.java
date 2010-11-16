package com.artagon.xacml.v3.spi.pip;

import java.util.Calendar;
import java.util.List;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationException;

public interface PolicyInformationPointContext 
{
	/**
	 * Gets current date/time
	 * 
	 * @return {@link Calendar} instance
	 * representing current date/time
	 */
	Calendar getCurrentDateTime();
	
	List<BagOfAttributeValues> getKeys() throws EvaluationException;
}
