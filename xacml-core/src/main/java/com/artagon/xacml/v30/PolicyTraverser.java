package com.artagon.xacml.v30;

public interface PolicyTraverser 
{
	void traverse(VariableDefinition var, PolicyVisitor v);
	void traverse(Condition condition, PolicyVisitor v);
	void traverse(Target target, PolicyVisitor v);
	void traverse(Match match, PolicyVisitor v);
	void traverse(MatchAllOf match, PolicyVisitor v);
	void traverse(MatchAnyOf match, PolicyVisitor v);
	void traverse(Rule rule, PolicyVisitor v);
	void traverse(PolicyIDReference ref, PolicyVisitor v);
	void traverse(Policy policy, PolicyVisitor v);
	void traverse(PolicySet policySet, PolicyVisitor v);
	void traverse(PolicySetIDReference ref, PolicyVisitor v);
	void traverse(ObligationExpression obligation, PolicyVisitor v);
	void traverse(AdviceExpression advice, PolicyVisitor v);
	void traverse(AttributeAssignmentExpression attribute, PolicyVisitor v);
	void traverse(PolicyDefaults policyDefaults, PolicyVisitor v);
	void traverse(PolicySetDefaults policySetDefaults, PolicyVisitor v);
	void traverse(CombinerParameter p, PolicyVisitor v);
	void traverse(CombinerParameters p, PolicyVisitor v);
	void traverse(RuleCombinerParameters p, PolicyVisitor v);
	void traverse(PolicyCombinerParameters p, PolicyVisitor v);
	void traverse(PolicySetCombinerParameters p, PolicyVisitor v);
	void traverse(PolicyIssuer p, PolicyVisitor v);

}
