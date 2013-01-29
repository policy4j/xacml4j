package org.xacml4j.v30.pdp;

import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.XacmlSyntaxException;

public class PolicySetDefaults extends BaseCompositeDecisionRuleDefaults
{
	public PolicySetDefaults(XPathVersion version){
		super(version);
	}
	
	public static PolicySetDefaults create(Object... objects)
			throws XacmlSyntaxException {
		if(objects != null && 
				objects.length > 0){
			if(objects[0] instanceof String){
				String value = (String)objects[0];
				XPathVersion v = XPathVersion.parse(value);
				if(v == null){
					throw new XacmlSyntaxException(
							"Unparsable XPath version=\"%s\"", value);
				}
				return new PolicySetDefaults(v);
			}
		}
		return null;
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

}
