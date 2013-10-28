package org.xacml4j.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class Reflections
{
	/**
	 * Finds all methods annotated with a given annotation
	 *
	 * @param clazz a class
	 * @param annotationClazz an annotation to look for
	 * @return a list of annotated methods in the given class
	 */
	public static List<Method> getAnnotatedMethods(
			Class<?> clazz, Class<? extends Annotation> annotationClazz)
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

	/**
	 * Finds a method with a given name in the given class
	 *
	 * @param clazz a class
	 * @param name a method name
	 * @return {@link Method} or <code>null</code> if method
	 * with a given name is note defined in a given class
	 */
	public static Method getMethod(Class<?> clazz, String name)
	{
		for(Method m : clazz.getDeclaredMethods()){
			if(m.getName().equals(name)){
				return m;
			}
		}
		return null;
	}
}

