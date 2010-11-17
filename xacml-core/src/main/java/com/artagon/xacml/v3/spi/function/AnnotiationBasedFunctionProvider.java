package com.artagon.xacml.v3.spi.function;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.invocation.DefaultInvocationFactory;
import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.sdk.XacmlFuncSpec;
import com.artagon.xacml.v3.sdk.XacmlFunctionProvider;
import com.google.common.base.Preconditions;

public class AnnotiationBasedFunctionProvider extends BaseFunctionProvider
{
	private JavaMethodToFunctionSpecConverter converter;
	
	public AnnotiationBasedFunctionProvider(Class<?> factoryClass) 
		throws Exception
	{
		Preconditions.checkNotNull(factoryClass);
		this.converter = new JavaMethodToFunctionSpecConverter(new DefaultInvocationFactory());
		List<FunctionSpec> functions = findFunctions(factoryClass, null);
		for(FunctionSpec spec : functions){
			add(spec);
		}
	}
	
	public AnnotiationBasedFunctionProvider(Object instance) 
		throws Exception
	{
		Preconditions.checkNotNull(instance);
		this.converter = new JavaMethodToFunctionSpecConverter(new DefaultInvocationFactory());
		List<FunctionSpec> functions = findFunctions(instance.getClass(), instance);
		for(FunctionSpec spec : functions){
			add(spec);
		}
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
