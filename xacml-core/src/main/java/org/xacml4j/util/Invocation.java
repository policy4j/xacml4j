package org.xacml4j.util;

public interface Invocation <V>
{
	V invoke(Object ... params) throws Exception;
}
