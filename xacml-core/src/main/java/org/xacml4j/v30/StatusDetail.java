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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;


/**
 * Represents XACML status details
 *
 * @author Giedrius Trumpickas
 */
public final class StatusDetail
{
	private List<Object> details;
	private String decisionRuleId;

	StatusDetail(){
	}

	public StatusDetail(Object details){
		this(null, Arrays.asList(details));
	}

	public StatusDetail(Collection<Object> details){
		this(null, details);
	}

	public StatusDetail(String decisionRuleId,
	                    Collection<Object> details){
		this.details = ImmutableList.copyOf(
				Objects.requireNonNull(details, "details"));
		this.decisionRuleId = decisionRuleId;
	}

	public String getDecisionRuleId() {
		return decisionRuleId;
	}

	public List<Object> getDetails(){
		return details;
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("details", details)
				.add("decisionRuleId", decisionRuleId)
				.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hash(details, decisionRuleId);
	}

}
