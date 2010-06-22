package com.artagon.xacml.v3;

import org.xml.sax.InputSource;

public interface XacmlPolicyReader 
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
	CompositeDecisionRule getPolicy(InputSource source) 
		throws PolicySyntaxException;
}
