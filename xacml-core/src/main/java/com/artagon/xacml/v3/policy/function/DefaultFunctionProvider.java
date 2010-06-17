package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.AggregatingFunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;

public class DefaultFunctionProvider extends AggregatingFunctionProvider
{
	public DefaultFunctionProvider()
	{
		add(new AnnotiationBasedFunctionProvider(ArithmeticFunctions.class));
		add(new AnnotiationBasedFunctionProvider(BagFunctions.class));
		add(new AnnotiationBasedFunctionProvider(DateTimeArithmeticFunctions.class));
		add(new AnnotiationBasedFunctionProvider(EqualityPredicates.class));
		add(new AnnotiationBasedFunctionProvider(LogicalFunctions.class));
		add(new AnnotiationBasedFunctionProvider(NonNumericComparisionFunctions.class));
		add(new AnnotiationBasedFunctionProvider(NumericConversionFunctions.class));
		add(new AnnotiationBasedFunctionProvider(NumericComparisionFunctions.class));
		add(new AnnotiationBasedFunctionProvider(RegularExpressionFunctions.class));
		add(new AnnotiationBasedFunctionProvider(SetFunctions.class));
		add(new AnnotiationBasedFunctionProvider(SpecialMatchFunctions.class));
		add(new AnnotiationBasedFunctionProvider(StringConversionFunctions.class));
		add(new AnnotiationBasedFunctionProvider(StringFunctions.class));
		add(new AnnotiationBasedFunctionProvider(XPathFunctions.class));
		add(new AnnotiationBasedFunctionProvider(HigherOrderFunctions.class));
	}
}

