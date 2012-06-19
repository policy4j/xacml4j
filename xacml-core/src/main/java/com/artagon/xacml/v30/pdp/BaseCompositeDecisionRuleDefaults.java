package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.XPathVersion;
import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRuleDefaults
	implements PolicyElement
{
	private XPathVersion version;

	protected BaseCompositeDecisionRuleDefaults(XPathVersion version){
		Preconditions.checkNotNull(version);
		this.version = version;
	}

	public XPathVersion getXPathVersion(){
		return version;
	}
}
