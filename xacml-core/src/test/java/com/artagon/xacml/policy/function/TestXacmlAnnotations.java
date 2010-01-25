package com.artagon.xacml.policy.function;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import com.artagon.xacml.policy.function.annotations.XacmlFuncSpec;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.util.Reflections;

public class TestXacmlAnnotations 
{
	
	@Test
	public void testFunctionAnnotations() throws Exception
	{
		List<Method> methods = Reflections.getAnnotatedMethods(EqualityPredicates.class, XacmlFuncSpec.class);
		for(Method m : methods){
			System.out.println(m.getName());
			System.out.println(m.getGenericReturnType());
			Type[] paramTypes = m.getGenericParameterTypes();
			Annotation[][] paramAnnotations = m.getParameterAnnotations();
			System.out.println(m.isVarArgs());
			System.out.println(paramAnnotations[0][0].toString());
			IntegerType.IntegerValue[] varArg = new IntegerType.IntegerValue[2];
			varArg[0] = XacmlDataType.INTEGER.create(1);
			varArg[1] = XacmlDataType.INTEGER.create(2);
			m.invoke(null, new Object[]{varArg});
		}
	}
}
