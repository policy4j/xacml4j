package org.xacml4j.v30.pdp;

import org.xacml4j.v30.Version;


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
