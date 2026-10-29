package org.xacml4j.v30.pdp;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PreparedPolicySets;
import org.xacml4j.v30.spi.repository.PolicyRepositoryListener;

final class PreparedPolicySetsListener implements PolicyRepositoryListener
{
	private final PreparedPolicySets prepared;

	PreparedPolicySetsListener(PreparedPolicySets prepared){
		this.prepared = prepared;
	}

	@Override
	public void policyAdded(Policy policy){
		prepared.invalidate();
	}

	@Override
	public void policyRemoved(Policy policy){
		prepared.invalidate();
	}

	@Override
	public void policySetAdded(PolicySet policy){
		prepared.invalidate();
	}

	@Override
	public void policySetRemoved(PolicySet policy){
		prepared.invalidate();
	}
}
