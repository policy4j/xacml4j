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

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.util.*;

import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;


/**
 * Represents XACML status details
 *
 * @author Giedrius Trumpickas
 */
public final class StatusDetail
{
	private List<Status> details;
	private String decisionRuleId;

	StatusDetail(){
	}

	public StatusDetail(String decisionRuleId,
	                    Status ...details){
		this(decisionRuleId, Arrays.asList(details));
	}

	public StatusDetail(Status ...details){
		this(null, Arrays.asList(details));
	}

	public StatusDetail(Collection<Status> details){
		this(null, details);
	}

	public StatusDetail(String decisionRuleId,
	                    Collection<Status> details){
		this.details = ImmutableList.copyOf(
				Objects.requireNonNull(details, "details"));
		this.decisionRuleId = decisionRuleId;
	}

	public String getDecisionRuleId() {
		return decisionRuleId;
	}

	public List<Status> getDetails(){
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
