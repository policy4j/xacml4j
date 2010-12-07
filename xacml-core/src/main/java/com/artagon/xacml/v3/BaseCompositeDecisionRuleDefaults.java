package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRuleDefaults extends XacmlObject 
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
