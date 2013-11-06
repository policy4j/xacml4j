package org.xacml4j.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 * In many applications, policies specify actions that MUST be performed,
 * either instead of, or in addition to, actions that MAY be performed.
 * XACML provides facilities to specify actions that MUST be performed in
 * conjunction with policy evaluation via {@link Obligation}.
 * There are no standard definitions for these actions in version 3.0 of XACML.
 * Therefore, bilateral agreement between a PAP and the PEP that will
 * enforce its policies is required for correct interpretation.
 * PEPs that conform to v3.0 of XACML are required to deny access unless
 * they understand and can discharge all of the {@link Obligation}  associated
 * with the applicable policy.
 *
 * @author Giedrius Trumpickas

 */
public class Obligation
	extends BaseDecisionRuleResponse
{
	Obligation(Builder b)
	{
		super(b);
	}

	public static Builder builder(String id, Effect appliesTo){
		return new Builder(id, appliesTo);
	}

	public static Builder builder(String id){
		return new Builder(id, null);
	}


	/**
	 * Combines this obligation attributes with a
	 * given obligation attributes
	 *
	 * @param o an obligation
	 * @return a new obligation instance with combined attributes
	 */
	public Obligation merge(Obligation o)
	{
		if( o == null){
			return this;
		}
		if(o == this){
			return this;
		}
		Preconditions.checkArgument(getId().equals(o.getId()));
		// HACK: not sure if we need this check
		Preconditions.checkArgument(Objects.equal(getFulfillOn(), o.getFulfillOn()));
		return new Builder(getId(), getFulfillOn())
		.attributes(getAttributes())
		.attributes(o.getAttributes())
		.build();
	}

	protected boolean equalsTo(Obligation o) {
		return super.equalsTo(o);
	}

	@Override
	public boolean equals(Object o){
		if (o == this) {
			return true;
		}

		return (o instanceof Obligation)
				&& ((Obligation)o).equalsTo(this);
	}

	public static class Builder extends BaseDecisionRuleResponse.Builder<Builder>
	{
		private Builder(String id, Effect effect){
			super(id, effect);
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public Obligation build(){
			return new Obligation(this);
		}
	}
}
