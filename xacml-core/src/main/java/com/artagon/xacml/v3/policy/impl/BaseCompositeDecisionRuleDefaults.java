package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

class BaseCompositeDecisionRuleDefaults extends XacmlObject
{
	private XPathVersion version;
	
	protected BaseCompositeDecisionRuleDefaults(XPathVersion version){
		Preconditions.checkNotNull(version);
		this.version = version;
	}
	
	public final XPathVersion getXPathVersion(){
		return version;
	}
}
