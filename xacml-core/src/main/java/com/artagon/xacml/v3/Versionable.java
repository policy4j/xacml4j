package com.artagon.xacml.v3;

import com.artagon.xacml.v3.policy.Version;

/**
 * An interface for versionable XACML entities
 * 
 * @author Giedrius Trumpickas
 */
public interface Versionable 
{
	/**
	 * Gets version information
	 * 
	 * @return version information
	 */
	Version getVersion();
}
