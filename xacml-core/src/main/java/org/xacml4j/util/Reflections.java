package org.xacml4j.util;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
	 * @return {@link Method} or {@code null} if method
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

