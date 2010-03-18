package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.PolicySetDefaults;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.artagon.xacml.v3.policy.XPathVersion;

public class DefaultPolicySetDefaults extends BaseCompositeDecisionRuleDefaults
	implements PolicySetDefaults
{
	public DefaultPolicySetDefaults(XPathVersion version){
		super(version);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

}
