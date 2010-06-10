package com.artagon.xacml.v3.policy.spi.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.policy.function.ArithmeticFunctions;
import com.artagon.xacml.v3.policy.function.BagFunctions;
import com.artagon.xacml.v3.policy.function.DateTimeArithmeticFunctions;
import com.artagon.xacml.v3.policy.function.EqualityPredicates;
import com.artagon.xacml.v3.policy.function.LogicalFunctions;
import com.artagon.xacml.v3.policy.function.NonNumericComparisionFunctions;
import com.artagon.xacml.v3.policy.function.NumericConversionFunctions;
import com.artagon.xacml.v3.policy.function.RegularExpressionFunctions;
import com.artagon.xacml.v3.policy.function.SetFunctions;
import com.artagon.xacml.v3.policy.function.SpecialMatchFunctions;
import com.artagon.xacml.v3.policy.function.StringConversionFunctions;
import com.artagon.xacml.v3.policy.function.StringFunctions;
import com.artagon.xacml.v3.policy.function.XPathFunctions;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;
import com.artagon.xacml.v3.policy.spi.FunctionProvidersRegistry;
import com.google.common.base.Preconditions;

public class DefaultFunctionProvidersRegistry implements FunctionProvidersRegistry 
{
	private Map<String, FunctionProvider> providers;
	
	public DefaultFunctionProvidersRegistry(){
		this.providers = new ConcurrentHashMap<String, FunctionProvider>();
		add(new ReflectionBasedFunctionProvider(ArithmeticFunctions.class));
		add(new ReflectionBasedFunctionProvider(BagFunctions.class));
		add(new ReflectionBasedFunctionProvider(DateTimeArithmeticFunctions.class));
		add(new ReflectionBasedFunctionProvider(EqualityPredicates.class));
		add(new ReflectionBasedFunctionProvider(LogicalFunctions.class));
		add(new ReflectionBasedFunctionProvider(NonNumericComparisionFunctions.class));
		add(new ReflectionBasedFunctionProvider(NumericConversionFunctions.class));
		add(new ReflectionBasedFunctionProvider(RegularExpressionFunctions.class));
		add(new ReflectionBasedFunctionProvider(SetFunctions.class));
		add(new ReflectionBasedFunctionProvider(SpecialMatchFunctions.class));
		add(new ReflectionBasedFunctionProvider(StringConversionFunctions.class));
		add(new ReflectionBasedFunctionProvider(StringFunctions.class));
		add(new ReflectionBasedFunctionProvider(XPathFunctions.class));
	}
	
	public void add(FunctionProvider provider)
	{
		for(String functionId : provider.getProvidedFunctions()){
			providers.put(functionId, provider);
		}
	}
	
	public void addAll(Iterable<FunctionProvider> providers){
		for(FunctionProvider provider : providers){
			add(provider);
		}
	}
	
	public FunctionSpec getFunction(String functionId) 
	{
		FunctionProvider provider = providers.get(functionId);
		if(provider == null){
			return null;
		}
		FunctionSpec spec = provider.getFunction(functionId);
		Preconditions.checkState(spec != null);
		return spec;
	}
}
