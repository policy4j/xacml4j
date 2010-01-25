package com.artagon.xacml.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Reflections
{
	public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClazz)
	{
		List<Method> methods = new LinkedList<Method>();		
		for(Method f : clazz.getDeclaredMethods()){						
			Annotation annotation = f.getAnnotation(annotationClazz);
			if(annotation != null){
				methods.add(f);
			}			
		}
		return methods;
	}
}

