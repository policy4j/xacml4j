package com.artagon.xacml.v3.policy.spi.function;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.artagon.xacml.v3.Value;
import com.google.common.base.Preconditions;

public class JDKReflectionBasedFunctionInvocation extends AbstractReflectionBasedFunctionInvocation
{
	private Method functionMethod;
	private Object instance;
	
	public JDKReflectionBasedFunctionInvocation(
			Object factoryInstance, 
			Class<?> factoryClass,
			Method m, 
			boolean evalContextRequired)
	{
		super(evalContextRequired);
		Preconditions.checkNotNull(m);
		Preconditions.checkNotNull(factoryClass);
		Preconditions.checkArgument(factoryInstance == null || 
			!Modifier.isStatic(m.getModifiers()));
		Preconditions.checkArgument(m.getDeclaringClass().equals(factoryClass));
		this.functionMethod = m;
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends Value> T invoke(Object ...params) throws Exception
	{
		return (T)functionMethod.invoke(instance, params);
	}
}