package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Version;

public interface PolicyFactory 
{
	AttributeValue createValue(String typeId, String value);
	
	PolicySetIDReference createPolicySetIDReference(
			String policyId, Version earliest, Version latest);
	
	FunctionSpec createFunction(String functionId);
	
	DecisionCombiningAlgorithm<CompositeDecisionRule> createPolicySetCombingingAlgorithm(String algorithmId);
	
	DecisionCombiningAlgorithm<Rule> createPolicyCombingingAlgorithm(String algorithmId);
	
}
