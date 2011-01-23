package com.artagon.xacml.v30;


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
