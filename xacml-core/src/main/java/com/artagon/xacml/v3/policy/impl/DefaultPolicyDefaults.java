package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.PolicyDefaults;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.XPathVersion;

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
