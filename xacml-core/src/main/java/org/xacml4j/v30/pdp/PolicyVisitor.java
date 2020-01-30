
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

/**
 * A XACML policy tree visitor
 *
 * @author Giedrius Trumpickas
 */
public interface PolicyVisitor
{
	default void visitEnter(VariableDefinition var){
		var.accept(this);
	}

	default void visitLeave(VariableDefinition var){
		var.accept(this);
	}

	default void visitEnter(Condition var){
		var.accept(this);
	}

	default void visitLeave(Condition var){
		var.accept(this);
	}

	default void visitEnter(Target var){
		var.accept(this);
	}

	default void visitLeave(Target var){
		var.accept(this);
	}

	void visitEnter(Match match);
	void visitLeave(Match match);

	void visitEnter(MatchAllOf match);
	void visitLeave(MatchAllOf match);

	void visitEnter(MatchAnyOf match);
	void visitLeave(MatchAnyOf match);

	void visitEnter(Rule rule);
	void visitLeave(Rule rule);

	void visitEnter(PolicyIDReference ref);
	void visitLeave(PolicyIDReference ref);

	void visitEnter(Policy policy);
	void visitLeave(Policy policy);

	void visitEnter(PolicySet policySet);
	void visitLeave(PolicySet policySet);

	void visitEnter(PolicySetIDReference ref);
	void visitLeave(PolicySetIDReference ref);

	void visitEnter(ObligationExpression obligation);
	void visitLeave(ObligationExpression obligation);


	void visitEnter(AdviceExpression advice);
	void visitLeave(AdviceExpression advice);

	void visitEnter(AttributeAssignmentExpression attribute);
	void visitLeave(AttributeAssignmentExpression attribute);

	void visitEnter(PolicyDefaults policyDefaults);
	void visitLeave(PolicyDefaults policyDefaults);

	void visitEnter(PolicySetDefaults policySetDefaults);
	void visitLeave(PolicySetDefaults policySetDefaults);


	void visitEnter(CombinerParameter p);
	void visitLeave(CombinerParameter p);
}
