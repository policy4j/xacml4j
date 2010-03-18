package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.PolicyDefaults;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.artagon.xacml.v3.policy.XPathVersion;

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
