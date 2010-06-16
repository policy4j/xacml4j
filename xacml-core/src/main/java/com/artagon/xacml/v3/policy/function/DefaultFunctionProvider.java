package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.spi.FunctionProviderRegistry;
import com.artagon.xacml.v3.policy.spi.function.ReflectionBasedFunctionProvider;

public class DefaultFunctionProvider extends FunctionProviderRegistry
{
	public DefaultFunctionProvider()
	{
		add(new ReflectionBasedFunctionProvider(ArithmeticFunctions.class));
		add(new ReflectionBasedFunctionProvider(BagFunctions.class));
		add(new ReflectionBasedFunctionProvider(DateTimeArithmeticFunctions.class));
		add(new ReflectionBasedFunctionProvider(EqualityPredicates.class));
		add(new ReflectionBasedFunctionProvider(LogicalFunctions.class));
		add(new ReflectionBasedFunctionProvider(NonNumericComparisionFunctions.class));
		add(new ReflectionBasedFunctionProvider(NumericConversionFunctions.class));
		add(new ReflectionBasedFunctionProvider(NumericComparisionFunctions.class));
		add(new ReflectionBasedFunctionProvider(RegularExpressionFunctions.class));
		add(new ReflectionBasedFunctionProvider(SetFunctions.class));
		add(new ReflectionBasedFunctionProvider(SpecialMatchFunctions.class));
		add(new ReflectionBasedFunctionProvider(StringConversionFunctions.class));
		add(new ReflectionBasedFunctionProvider(StringFunctions.class));
		add(new ReflectionBasedFunctionProvider(XPathFunctions.class));
	}
}

