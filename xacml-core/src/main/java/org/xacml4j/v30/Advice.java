package org.xacml4j.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * In some applications it is helpful to specify supplemental
 * information about a decision. XACML provides facilities
 * to specify supplemental information about a decision with
 * the {@link Advice}. Such advice may be safely ignored by the PEP.
 *
 * @author Giedrius Trumpickas
 */
public class Advice extends BaseDecisionRuleResponse
{
	private Advice(Builder b){
		super(b);
	}

	public static Builder builder(String id, Effect fullFillOn){
		return new Builder(id, fullFillOn);
	}

	public static Builder builder(String id){
		return new Builder(id, null);
	}

	protected boolean equalsTo(Advice o) {
		return super.equalsTo(o);
	}

	@Override
	public boolean equals(Object o){
		if (o == this) {
			return true;
		}

		return (o instanceof Advice)
				&& ((Advice)o).equalsTo(this);
	}

	/**
	 * Combines this advice attributes with a
	 * given advice attributes
	 *
	 * @param a an advice
	 * @return a new advice instance with combined attributes
	 */
	public Advice merge(Advice a)
	{
		Preconditions.checkArgument(a.getId().equals(getId()));
		Preconditions.checkArgument(Objects.equal(getFulfillOn(), a.getFulfillOn()));
		return new Builder(getId(), getFulfillOn())
		.attributes(getAttributes())
		.attributes(a.getAttributes()).build();

	}

	public static class Builder extends BaseDecisionRuleResponse.Builder<Builder>
	{
		private Builder(String id, Effect effect){
			super(id, effect);
		}

		public Advice build(){
			return new Advice(this);
		}

		@Override
		protected Builder getThis() {
			return this;
		}
	}
}
