package org.xacml4j.util;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Reflections
{
	private final static Logger LOG = LoggerFactory.getLogger(Reflections.class);

	/** Private constructor for utility class */
	private Reflections() {}

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
		return Arrays.
				stream(clazz.getDeclaredMethods())
				.filter(m->m.isAnnotationPresent(annotationClazz))
				.collect(Collectors.toList());
	}

	public static <ClassType, FieldType>  Set<FieldType> getDeclaredStaticFields(
			Class<ClassType> targetClass, Class<FieldType> fieldType){

		return Arrays.stream(targetClass.getFields())
				.filter(f-> Modifier.isStatic(f.getModifiers()) && (
						fieldType.isAssignableFrom(f.getDeclaringClass())))
				.map(v->mapFieldValue(v)).map(v->fieldType.cast(v)).collect(Collectors.toSet());
	}

	private static <T>  T mapFieldValue(Field f)
	{
		try{
			return (T) f.get(null);
		}catch(IllegalAccessException e){
			LOG.warn(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Finds a method with a given name in the given class
	 *
	 * @param clazz a class
	 * @param name a method name
	 * @return {@link Method} or {@code null} if method
	 * with a given name is note defined in a given class
	 */
	public static Optional<Method> getMethod(Class<?> clazz, String name)
	{
		return Arrays
				.stream(clazz.getDeclaredMethods())
				.filter(m->m.getName().equalsIgnoreCase(name))
				.findFirst();
	}
}

