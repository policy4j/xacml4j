
package com.artagon.xacml.v30;


public interface PolicyVisitor 
{
	void visitEnter(VariableDefinition var);
	void visitLeave(VariableDefinition var);
	
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
	
	void visitEnter(AttributeAssignmentExpression attribute);
	void visitLeave(AttributeAssignmentExpression attribute);
	
	void visitEnter(PolicyDefaults policyDefaults);
	void visitLeave(PolicyDefaults policyDefaults);
	
	void visitEnter(PolicySetDefaults policySetDefaults);
	void visitLeave(PolicySetDefaults policySetDefaults);
	
	
	void visitEnter(CombinerParameter p);
	void visitLeave(CombinerParameter p);
	
	
	void visitEnter(CombinerParameters p);
	void visitLeave(CombinerParameters p);
	
	void visitEnter(RuleCombinerParameters p);
	void visitLeave(RuleCombinerParameters p);
	
	void visitEnter(PolicyCombinerParameters p);
	void visitLeave(PolicyCombinerParameters p);
	
	void visitEnter(PolicySetCombinerParameters p);
	void visitLeave(PolicySetCombinerParameters p);
	
	void visitEnter(PolicyIssuer p);
	void visitLeave(PolicyIssuer p);
}
