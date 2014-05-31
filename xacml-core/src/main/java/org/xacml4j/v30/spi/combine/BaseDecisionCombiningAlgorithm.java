package org.xacml4j.v30.spi.combine;

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

import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public abstract class BaseDecisionCombiningAlgorithm <D extends DecisionRule>
	implements DecisionCombiningAlgorithm <D>
{
	private final String algorithmId;

	/**
	 * Creates decision combining algorithm with a
	 * given algorithm identifier
	 * @param algorithmId an algorithm identifier
	 */
	protected BaseDecisionCombiningAlgorithm(String algorithmId){
		Preconditions.checkNotNull(algorithmId);
		this.algorithmId = algorithmId;
	}

	/**
	 * Gets decision algorithm identifier
	 *
	 * @return decision algorithm identifier
	 */
	@Override
	public final String getId(){
		return algorithmId;
	}

	@Override
	public final int hashCode(){
		return algorithmId.hashCode();
	}

	@Override
	public final boolean equals(Object o){
		if (o == this) {
			return true;
		}

		if(!(o instanceof BaseDecisionCombiningAlgorithm<?>)){
			return false;
		}

		BaseDecisionCombiningAlgorithm<?> a = (BaseDecisionCombiningAlgorithm<?>)o;
		return algorithmId.equals(a.algorithmId);
	}

	@Override
	public final String toString() {
		return Objects
				.toStringHelper(this)
				.add("algorithmId", algorithmId)
				.toString();
	}
}
