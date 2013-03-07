package org.xacml4j.v30.spi.function;

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
import org.xacml4j.v30.policy.function.NonNumericComparisionFunctions;
import org.xacml4j.v30.policy.function.NumericComparisionFunctions;
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
	 * Adds function provider from a given anno instance
	 * 
	 * @param clazz an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder fromInstance(Object p)
	{
		Preconditions.checkNotNull(p);
		try{
			return (p instanceof FunctionProvider)?
					provider((FunctionProvider)p):
						provider(
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
	public FunctionProviderBuilder defaultFunctions()
	{
		fromClass(ArithmeticFunctions.class);
		fromClass(BagFunctions.class);
		fromClass(DateTimeArithmeticFunctions.class);
		fromClass(EqualityPredicates.class);
		fromClass(LogicalFunctions.class);
		fromClass(NonNumericComparisionFunctions.class);
		fromClass(NumericConversionFunctions.class);
		fromClass(NumericComparisionFunctions.class);
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
