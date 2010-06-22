package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import org.xml.sax.InputSource;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.PolicySyntaxException;

public interface XacmlPolicyUnmarshaller 
{
	/**
	 * Creates policy from a given {@link InputSource}
	 * 
	 * @param source an input source
	 * @return {@link 
	 * @exception PolicySyntaxException if an error
	 * occurs while parsing policy definition
	 * @return {@link CompositeDecisionRule} instance
	 */
	CompositeDecisionRule getPolicy(Object source) 
		throws PolicySyntaxException, IOException;
}
