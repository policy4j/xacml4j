package org.xacml4j.v30.policy;

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


public class PolicyDefaults extends BaseCompositeDecisionRuleDefaults
{
	private PolicyDefaults(Builder b) {
		super(b);
	}

	public static Builder builder(){
		return new Builder();
	}

	public void accept(PolicyTreeVisitor v) {
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
