package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRuleDefaults extends XacmlObject 
	implements PolicyElement
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
