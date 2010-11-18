package com.artagon.xacml.invocation;

import java.lang.reflect.Method;

import com.google.common.base.Preconditions;

public final class DefaultInvocationFactory implements InvocationFactory
{	
	@Override
	public <T> Invocation<T> create(final Object instance, final Method m) 
	{
		Preconditions.checkArgument(m != null);
		return new Invocation<T>(){
			@SuppressWarnings("unchecked")
			@Override
			public T invoke(Object ... params) throws Exception {
				return (T)m.invoke(instance, params);
			}
			
		};
	}
}
