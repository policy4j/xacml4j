package com.artagon.xacml.v3;



public class PolicyDefaults extends BaseCompositeDecisionRuleDefaults
{
	public PolicyDefaults(XPathVersion version) {
		super(version);
	}
	
	public static PolicyDefaults create(Object... objects)
			throws XacmlSyntaxException 
	{
		if(objects != null && 
				objects.length > 0){
			if(objects[0] instanceof String){
				String value = (String)objects[0];
				XPathVersion v = XPathVersion.parse(value);
				if(v == null){
					throw new XacmlSyntaxException(
							"Unparsable XPath version=\"%s\"", value);
				}
				return new PolicyDefaults(v);
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
