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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;

public class PolicyResolutionException extends EvaluationException
{
	private static final long serialVersionUID = 5535690322056670601L;

	private DecisionRuleEvaluationContext context;

	public PolicyResolutionException(DecisionRuleEvaluationContext context,
			String template, Object... arguments) {
		super(Status.processingError().build(), template, arguments);
		this.context = context;
	}

	public PolicyResolutionException(DecisionRuleEvaluationContext context,
			Throwable cause, String message,
			Object... arguments) {
		super(Status.processingError().build(), cause, message, arguments);
		this.context = context;
	}

	public PolicyResolutionException(DecisionRuleEvaluationContext context,
			Throwable cause) {
		super(Status.processingError().build(), cause);
		this.context = context;
	}

	public CompositeDecisionRuleIDReference getPolicyIDReference(){
		return context.getCurrentPolicyIDReference();
	}

	public CompositeDecisionRuleIDReference getPolicySetIDReference(){
		return context.getCurrentPolicySetIDReference();
	}
}
