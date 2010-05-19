
package com.artagon.xacml.v3.policy;



public interface PolicyVisitor
{
	
	void visitEnter(AttributeValue attr);
	void visitLeave(AttributeValue attr);
	
	void visitEnter(AttributeAssignment attr);
	void visitLeave(AttributeAssignment attr);
	
	void visitEnter(AttributeDesignator designator);
	void visitLeave(AttributeDesignator designator);
	
	void visitEnter(AttributeSelector selector);
	void visitLeave(AttributeSelector selector);
	
	void visitEnter(FunctionReference function);
	void visitLeave(FunctionReference function);
	
	void visitEnter(BagOfAttributeValues<?> bag);
	void visitLeave(BagOfAttributeValues<?> bag);
	
	void visitEnter(VariableReference var);
	void visitLeave(VariableReference var);
	
	void visitEnter(VariableDefinition var);
	void visitLeave(VariableDefinition var);
	
	void visitEnter(Apply apply);
	void visitLeave(Apply apply);
	
	void visitEnter(Condition condition);
	void visitLeave(Condition condition);
	
	void visitEnter(Target target);
	void visitLeave(Target target);
	
	void visitEnter(Match match);
	void visitLeave(Match match);
	
	void visitEnter(MatchAllOf match);
	void visitLeave(MatchAllOf match);
	
	void visitEnter(MatchAnyOf match);
	void visitLeave(MatchAnyOf match);
	
	void visitEnter(Rule rule);
	void visitLeave(Rule rule);
	
	void visitEnter(DecisionCombiningAlgorithm<? extends DecisionRule> combine);
	void visitLeave(DecisionCombiningAlgorithm<? extends DecisionRule> combine);
	
	void visitEnter(PolicyIDReference ref);
	void visitLeave(PolicyIDReference ref);
	
	void visitEnter(Policy policy);
	void visitLeave(Policy policy);
	
	void visitEnter(PolicySet policySet);
	void visitLeave(PolicySet policySet);
	
	void visitEnter(PolicySetIDReference ref);
	void visitLeave(PolicySetIDReference ref);
	
	void visitEnter(ObligationExpression obligation);
	void visitLeave(ObligationExpression obligation);
	
	void visitEnter(AdviceExpression advice);
	void visitLeave(AdviceExpression advice);
	
	void visitEnter(AttributeAssigmentExpression attribute);
	void visitLeave(AttributeAssigmentExpression attribute);
	
	void visitEnter(PolicyDefaults policyDefaults);
	void visitLeave(PolicyDefaults policyDefaults);
	
	void visitEnter(PolicySetDefaults policySetDefaults);
	void visitLeave(PolicySetDefaults policySetDefaults);
}
