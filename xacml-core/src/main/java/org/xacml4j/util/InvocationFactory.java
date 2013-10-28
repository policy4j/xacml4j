package org.xacml4j.util;

import java.lang.reflect.Method;

public interface InvocationFactory
{
	<T> Invocation<T> create(Object instance, Method m);
}
