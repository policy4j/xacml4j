package org.xacml4j.v30.policy.function.impl;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.util.Reflections;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;

import com.google.common.base.Preconditions;


/**
 * Annotation based function provider
 *
 * @author Giedrius Trumpickas
 */
public final class AnnotationBasedFunctionProvider extends BaseFunctionProvider
{
	private final static Logger LOG = LoggerFactory.getLogger(AnnotationBasedFunctionProvider.class);

	private AnnotationBasedFunctionProvider(
			Class<?> factoryClass,
			Object instance,
			FunctionInvocationFactory invocationFactory)
	{
		super(findFunctions(factoryClass, instance,
				invocationFactory).collect(Collectors.toList()));
	}

	public static Collection<FunctionProvider> toProviders(FunctionInvocationFactory factory, Class<?> ... clazz){
		return Arrays.stream(clazz)
				.map(c->new AnnotationBasedFunctionProvider(c, null, factory))
				.collect(Collectors.toList());
	}

	public static Collection<FunctionProvider> toProviders(FunctionInvocationFactory factory, Object instance){
		Objects.requireNonNull(instance,  "instance");
		return Collections.singleton(new AnnotationBasedFunctionProvider(instance.getClass(), instance, factory));
	}

	private static Stream<FunctionSpec> findFunctions(Class<?> clazz, Object instance, FunctionInvocationFactory factory)
		throws SyntaxException
	{
		Preconditions.checkArgument(clazz.getAnnotation(XacmlFunctionProvider.class) != null,
				"Function provider=\"%s\" must have provider annotation", clazz.getName());
		LOG.debug("function class={} instance={}", clazz, instance);
		JavaMethodToFunctionSpecConverter converter = new JavaMethodToFunctionSpecConverter(factory);
		return Reflections.getAnnotatedMethods(clazz, XacmlFuncSpec.class)
				.stream().map(v->{
					FunctionSpec spec = converter.createFunctionSpec(v, instance);
					return spec;
				});
	}
}
