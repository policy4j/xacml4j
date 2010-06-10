package com.artagon.xacml.v3;



public class PolicyDefaults extends BaseCompositeDecisionRuleDefaults
{
	public PolicyDefaults(XPathVersion version) {
		super(version);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
	
	
}
