package com.artagon.xacml.v3.spi.function;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.util.Reflections;
import com.artagon.xacml.v3.FunctionSpec;
import com.google.common.base.Preconditions;

public class AnnotiationBasedFunctionProvider extends BaseFunctionProvider
{
	private JavaMethodToFunctionSpecConverter converter;
	
	public AnnotiationBasedFunctionProvider(Class<?> factoryClass)
	{
		Preconditions.checkNotNull(factoryClass);
		this.converter = new JavaMethodToFunctionSpecConverter();
		List<FunctionSpec> functions = findFunctions(factoryClass);
		for(FunctionSpec spec : functions){
			add(spec);
		}
	}
	
	private List<FunctionSpec> findFunctions(Class<?> clazz)
	{
		List<FunctionSpec> specs = new LinkedList<FunctionSpec>();
		List<Method> methods  = Reflections.getAnnotatedMethods(clazz, XacmlFunc.class);
		for(final Method m : methods){
			specs.add(converter.createFunctionSpec(m));
		}
		return specs;
	}

}
