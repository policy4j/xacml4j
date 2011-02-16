package com.artagon.xacml.v30.spi.function;

import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.util.DefaultInvocationFactory;
import com.artagon.xacml.util.InvocationFactory;
import com.artagon.xacml.v30.policy.function.ArithmeticFunctions;
import com.artagon.xacml.v30.policy.function.BagFunctions;
import com.artagon.xacml.v30.policy.function.DateTimeArithmeticFunctions;
import com.artagon.xacml.v30.policy.function.EqualityPredicates;
import com.artagon.xacml.v30.policy.function.HigherOrderFunctions;
import com.artagon.xacml.v30.policy.function.LogicalFunctions;
import com.artagon.xacml.v30.policy.function.NonNumericComparisionFunctions;
import com.artagon.xacml.v30.policy.function.NumericComparisionFunctions;
import com.artagon.xacml.v30.policy.function.NumericConversionFunctions;
import com.artagon.xacml.v30.policy.function.RegularExpressionFunctions;
import com.artagon.xacml.v30.policy.function.SetFunctions;
import com.artagon.xacml.v30.policy.function.SpecialMatchFunctions;
import com.artagon.xacml.v30.policy.function.StringConversionFunctions;
import com.artagon.xacml.v30.policy.function.StringFunctions;
import com.artagon.xacml.v30.policy.function.XPathFunctions;
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
	 * Adds function provider from a given anno instance
	 * 
	 * @param clazz an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder withFunctionsFrom(Object p)
	{
		Preconditions.checkNotNull(p);
		try{
			return (p instanceof FunctionProvider)?
					withFunctions((FunctionProvider)p):
						withFunctions(
								new AnnotiationBasedFunctionProvider(p, 
										invocationFactory));
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Adds standard XACML 3.0 functions
	 * 
	 * @param clazz an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder withDefaultFunctions()
	{
		withFunctions(ArithmeticFunctions.class);
		withFunctions(BagFunctions.class);
		withFunctions(DateTimeArithmeticFunctions.class);
		withFunctions(EqualityPredicates.class);
		withFunctions(LogicalFunctions.class);
		withFunctions(NonNumericComparisionFunctions.class);
		withFunctions(NumericConversionFunctions.class);
		withFunctions(NumericComparisionFunctions.class);
		withFunctions(RegularExpressionFunctions.class);
		withFunctions(SetFunctions.class);
		withFunctions(SpecialMatchFunctions.class);
		withFunctions(StringConversionFunctions.class);
		withFunctions(StringFunctions.class);
		withFunctions(XPathFunctions.class);
		withFunctions(HigherOrderFunctions.class);
		return this;
	}
	
	/**
	 * Adds function provider from a given annotated class
	 * 
	 * @param clazz an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder withFunctions(Class<?> clazz)
	{
		Preconditions.checkNotNull(clazz);
		try{
			return withFunctions(
					new AnnotiationBasedFunctionProvider(clazz, invocationFactory));
		}catch(Exception e){
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Adds function provider from a given annotated class
	 * 
	 * @param clazz an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder withFunctions(Iterable<FunctionProvider> provider){
		for(FunctionProvider p : providers){
			withFunctions(p);
		}
		return this;
	}
	
	public FunctionProviderBuilder withFunctions(FunctionProvider p){
		Preconditions.checkNotNull(p);
		this.providers.add(p);
		return this;
	}
	
	public FunctionProvider build(){
		return new AggregatingFunctionProvider(providers);
	}
	
}
