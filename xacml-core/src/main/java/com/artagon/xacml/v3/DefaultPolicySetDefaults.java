package com.artagon.xacml.v3;


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
