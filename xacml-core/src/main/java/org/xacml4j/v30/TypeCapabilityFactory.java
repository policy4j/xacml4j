package org.xacml4j.v30;

import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;

public interface TypeCapabilityFactory<T extends TypeCapability>
{
	Class<T> getCapabilityType();
	Collection<T> getCapabilities();
	Optional<T> forType(AttributeValueType type);
}
