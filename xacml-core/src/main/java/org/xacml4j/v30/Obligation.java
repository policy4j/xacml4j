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
	 * Combines this obligations category with a
	 * given obligations category
	 *
	 * @param o an obligations
	 * @return a new obligations instance with combined category
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
