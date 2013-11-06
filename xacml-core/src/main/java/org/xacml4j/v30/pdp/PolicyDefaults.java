package org.xacml4j.v30.pdp;


public class PolicyDefaults extends BaseCompositeDecisionRuleDefaults
{
	private PolicyDefaults(Builder b) {
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
		if(!(o instanceof PolicyDefaults)){
			return false;
		}
		PolicyDefaults d = (PolicyDefaults)o;
		return values.equals(d.values);
	}

	public static class Builder extends BaseCompositeDecisionRuleDefaults.Builder<Builder>
	{
		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicyDefaults build(){
			return new PolicyDefaults(this);
		}
	}
}
