package com.artagon.xacml.v30;

import com.artagon.xacml.v30.core.XPathVersion;
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
