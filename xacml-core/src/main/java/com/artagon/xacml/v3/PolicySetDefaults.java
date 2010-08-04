package com.artagon.xacml.v3;


public class PolicySetDefaults extends BaseCompositeDecisionRuleDefaults
{
	public PolicySetDefaults(XPathVersion version){
		super(version);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

}
