package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.PolicySyntaxException;

public interface PolicyUnmarshaller 
{
	/**
	 * Creates policy from a given {@link Object}
	 * 
	 * @param source an input source
	 * @return {@link CompositeDecisionRule} instance
	 * @exception PolicySyntaxException if an error
	 * occurs while parsing policy definition
	 * @return {@link CompositeDecisionRule} instance
	 */
	CompositeDecisionRule unmarshall(Object source) 
		throws PolicySyntaxException, IOException;
}
