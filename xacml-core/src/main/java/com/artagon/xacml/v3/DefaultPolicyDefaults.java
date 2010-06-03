package com.artagon.xacml.v3;


public class DefaultPolicyDefaults extends BaseCompositeDecisionRuleDefaults 
	implements PolicyDefaults {

	public DefaultPolicyDefaults(XPathVersion version) {
		super(version);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
	
	
}
