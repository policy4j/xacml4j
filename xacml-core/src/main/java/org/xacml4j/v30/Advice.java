package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
public final class Advice extends DecisionRuleResponse
{
	private Advice(Builder b){
		super(b);
	}

	public static Builder builder(String id, Effect fullFillOn){
		return new Builder(id, fullFillOn);
	}

	public static Builder from(Advice a){
		return new Builder(a.getId(), a.getFulfillOn()).attributes(a.getAttributes());
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
	 * @return a new advice defaultProvider with combined attributes
	 */
	public Advice merge(DecisionRuleResponse v)
	{
		if(v == null){
			return this;
		}
		if(v == this){
			return this;
		}
		if(!(v instanceof Advice)){
			return this;
		}
		if(!(getId().equals(v.getId()))){
			return this;
		}
		Advice a = Advice.class.cast(v);
		return Advice.from(this)
				.attributes(a.getAttributes())
				.build();
	}

	public static class Builder extends DecisionRuleResponse.Builder<Builder>
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
