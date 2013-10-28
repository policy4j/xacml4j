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

	@Override
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(o == this){
			return true;
		}
		if(!(o instanceof Advice)){
			return false;
		}
		Advice a = (Advice)o;
		return id.equals(a.id) &&
				attributes.equals(a.attributes);
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
		Preconditions.checkArgument(Objects.equal(getFullfillOn(), a.getFullfillOn()));
		return new Advice.Builder(getId(), getFullfillOn())
		.attributes(getAttributes())
		.attributes(a.getAttributes()).build();

	}

	public static class Builder extends BaseBuilder<Builder>
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
