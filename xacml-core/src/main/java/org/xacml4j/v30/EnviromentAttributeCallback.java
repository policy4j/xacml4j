package org.xacml4j.v30;

import java.util.Collection;

/**
 * A callback used by {@link PDP} to retrieve environment 
 * related attributes
 * 
 * @author Giedrius Trumpickas
 */
public interface EnviromentAttributeCallback 
{
	Collection<Attribute> getEnviromentAttributes();
}
