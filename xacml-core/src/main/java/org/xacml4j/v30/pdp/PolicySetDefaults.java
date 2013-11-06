package org.xacml4j.v30.pdp;

public class PolicySetDefaults extends BaseCompositeDecisionRuleDefaults
{
	private PolicySetDefaults(Builder b) {
		super(b);
	}

	public static Builder builder(){
		return new Builder();
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof PolicySetDefaults)){
			return false;
		}
		PolicySetDefaults d = (PolicySetDefaults)o;
		return values.equals(d.values);
	}

	public static class Builder extends BaseCompositeDecisionRuleDefaults.Builder<Builder>
	{
		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicySetDefaults build(){
			return new PolicySetDefaults(this);
		}
	}
}
