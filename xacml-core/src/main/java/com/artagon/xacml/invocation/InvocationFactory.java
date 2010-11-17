package com.artagon.xacml.invocation;

import java.lang.reflect.Method;

public interface InvocationFactory 
{
	<T> Invocation<T> create(Object instance, Method m);
}
