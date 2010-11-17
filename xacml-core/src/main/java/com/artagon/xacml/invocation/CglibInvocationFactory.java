package com.artagon.xacml.invocation;

import java.lang.reflect.Method;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

public final class CglibInvocationFactory implements InvocationFactory
{
	@Override
	public <T> Invocation<T> create(final Object instance, final Method m) {
		return new Invocation<T>() 
		{
			private FastClass fastClass;
			private FastMethod fastMethod;
	
			{
				this.fastClass = FastClass.create(m.getDeclaringClass());
				this.fastMethod = fastClass.getMethod(m);
			}

			@SuppressWarnings("unchecked")
			@Override
			public T invoke(Object... params) throws Exception {
				return (T)fastMethod.invoke(instance, params);
			}
		};
	}
	
}
