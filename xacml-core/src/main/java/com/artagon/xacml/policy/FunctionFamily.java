package com.artagon.xacml.policy;

/**
 * Represents a specification which
 * defines a family or set of functions
 * 
 * @author Giedrius Trumpickas
 */
public interface FunctionFamily 
{
	/**
	 * Tests if a given function is a
	 * member of this family
	 * 
	 * @param spec a function specification
	 * @return <code>true</code> if a
	 * function is a member of this family
	 */
	boolean isMemeberOf(FunctionSpec spec);
}
