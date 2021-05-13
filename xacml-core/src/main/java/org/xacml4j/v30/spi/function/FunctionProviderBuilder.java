package org.xacml4j.v30.spi.function;

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

import java.util.LinkedList;
import java.util.List;

import org.xacml4j.util.DefaultInvocationFactory;
import org.xacml4j.util.InvocationFactory;
import org.xacml4j.v30.policy.function.ArithmeticFunctions;
import org.xacml4j.v30.policy.function.BagFunctions;
import org.xacml4j.v30.policy.function.DateTimeArithmeticFunctions;
import org.xacml4j.v30.policy.function.EqualityPredicates;
import org.xacml4j.v30.policy.function.HigherOrderFunctions;
import org.xacml4j.v30.policy.function.LogicalFunctions;
import org.xacml4j.v30.policy.function.NonNumericComparisonFunctions;
import org.xacml4j.v30.policy.function.NumericComparisonFunctions;
import org.xacml4j.v30.policy.function.NumericConversionFunctions;
import org.xacml4j.v30.policy.function.RegularExpressionFunctions;
import org.xacml4j.v30.policy.function.SetFunctions;
import org.xacml4j.v30.policy.function.SpecialMatchFunctions;
import org.xacml4j.v30.policy.function.StringConversionFunctions;
import org.xacml4j.v30.policy.function.StringFunctions;
import org.xacml4j.v30.policy.function.XPathFunctions;

import com.google.common.base.Preconditions;

public final class FunctionProviderBuilder
{
	private List<FunctionProvider> providers;
	private InvocationFactory invocationFactory;

	private FunctionProviderBuilder(
			InvocationFactory invocationFactory){
		Preconditions.checkNotNull(invocationFactory);
		this.providers = new LinkedList<FunctionProvider>();
		this.invocationFactory = invocationFactory;
	}

	private FunctionProviderBuilder(){
		this(new DefaultInvocationFactory());
	}

	public static FunctionProviderBuilder builder(){
		return new FunctionProviderBuilder();
	}

	public static FunctionProviderBuilder builder(InvocationFactory invocation){
		return new FunctionProviderBuilder(invocation);
	}

	/**
	 * Adds function provider from a given annotated instance
	 *
	 * @param p an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder fromInstance(Object p)
	{
		Preconditions.checkNotNull(p);
		try{
			return (p instanceof FunctionProvider)?
					provider((FunctionProvider)p):
						provider(
								new AnnotationBasedFunctionProvider(
										p,
										invocationFactory));
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds standard XACML 3.0 functions
	 *
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder defaultFunctions()
	{
		fromClass(ArithmeticFunctions.class);
		fromClass(BagFunctions.class);
		fromClass(DateTimeArithmeticFunctions.class);
		fromClass(EqualityPredicates.class);
		fromClass(LogicalFunctions.class);
		fromClass(NonNumericComparisonFunctions.class);
		fromClass(NumericConversionFunctions.class);
		fromClass(NumericComparisonFunctions.class);
		fromClass(RegularExpressionFunctions.class);
		fromClass(SetFunctions.class);
		fromClass(SpecialMatchFunctions.class);
		fromClass(StringConversionFunctions.class);
		fromClass(StringFunctions.class);
		fromClass(XPathFunctions.class);
		fromClass(HigherOrderFunctions.class);
		return this;
	}

	/**
	 * Adds function provider from a given annotated class
	 *
	 * @param clazz an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder fromClass(Class<?> clazz)
	{
		Preconditions.checkNotNull(clazz);
		try{
			return provider(
					new AnnotationBasedFunctionProvider(clazz, invocationFactory));
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds function provider from a given annotated class
	 *
	 * @param provider an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder providers(Iterable<FunctionProvider> provider){
		for(FunctionProvider p : providers){
			provider(p);
		}
		return this;
	}

	public FunctionProviderBuilder provider(FunctionProvider p){
		Preconditions.checkNotNull(p);
		this.providers.add(p);
		return this;
	}

	public FunctionProvider build(){
		Preconditions.checkState(!providers.isEmpty(),
				"At least one function provider must be specified");
		return new AggregatingFunctionProvider(providers);
	}

}
