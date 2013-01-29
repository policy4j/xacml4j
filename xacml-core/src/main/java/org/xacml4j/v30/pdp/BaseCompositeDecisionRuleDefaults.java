package org.xacml4j.v30.pdp;

import org.xacml4j.v30.XPathVersion;

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
