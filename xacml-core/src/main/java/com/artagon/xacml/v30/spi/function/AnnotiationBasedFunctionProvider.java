package com.artagon.xacml.v30.spi.function;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.util.CglibInvocationFactory;
import com.artagon.xacml.util.DefaultInvocationFactory;
import com.artagon.xacml.util.InvocationFactory;
import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v30.FunctionSpec;
import com.artagon.xacml.v30.XacmlSyntaxException;
import com.google.common.base.Preconditions;

public final class AnnotiationBasedFunctionProvider extends BaseFunctionProvider
{
	private JavaMethodToFunctionSpecConverter converter;
	
	public AnnotiationBasedFunctionProvider(Class<?> factoryClass, 
			InvocationFactory invocationFactory) 
		throws Exception
	{
		Preconditions.checkNotNull(factoryClass);
		Preconditions.checkNotNull(invocationFactory);
		this.converter = new JavaMethodToFunctionSpecConverter(invocationFactory);
		List<FunctionSpec> functions = findFunctions(factoryClass, null);
		for(FunctionSpec spec : functions){
			add(spec);
		}
	}
	
	public AnnotiationBasedFunctionProvider(Class<?> clazz) 
		throws Exception{
		this(clazz, new CglibInvocationFactory());
	}
	
	public AnnotiationBasedFunctionProvider(Object instance, InvocationFactory invocationFactory) 
		throws Exception
	{
		Preconditions.checkNotNull(instance);
		Preconditions.checkNotNull(invocationFactory);
		this.converter = new JavaMethodToFunctionSpecConverter(invocationFactory);
		List<FunctionSpec> functions = findFunctions(instance.getClass(), instance);
		for(FunctionSpec spec : functions){
			add(spec);
		}
	}
	
	public AnnotiationBasedFunctionProvider(Object instance) throws Exception{
		this(instance, new DefaultInvocationFactory());
	}
		
	private List<FunctionSpec> findFunctions(Class<?> clazz, Object instance) 
		throws XacmlSyntaxException
	{
		Preconditions.checkArgument(clazz.getAnnotation(XacmlFunctionProvider.class) != null, 
				"Function provider=\"%s\" must have provider annotiation", clazz.getName());
		List<FunctionSpec> specs = new LinkedList<FunctionSpec>();
		List<Method> methods  = Reflections.getAnnotatedMethods(clazz, XacmlFuncSpec.class);
		for(final Method m : methods){
			specs.add(converter.createFunctionSpec(m, instance));
		}
		return specs;
	}
}
