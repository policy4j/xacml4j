package org.xacml4j.v30.pdp;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

public class PolicyVisitorSupport implements PolicyVisitor
{

	@Override
	public void visitEnter(VariableDefinition var) {
	}

	@Override
	public void visitLeave(VariableDefinition var) {
	}

	@Override
	public void visitEnter(Condition condition) {
	}

	@Override
	public void visitLeave(Condition condition) {
	}

	@Override
	public void visitEnter(Target target) {
	}

	@Override
	public void visitLeave(Target target) {
	}

	@Override
	public void visitEnter(Match match) {
	}

	@Override
	public void visitLeave(Match match) {
	}

	@Override
	public void visitEnter(MatchAllOf match) {
	}

	@Override
	public void visitLeave(MatchAllOf match) {
	}

	@Override
	public void visitEnter(MatchAnyOf match) {
	}

	@Override
	public void visitLeave(MatchAnyOf match) {
	}

	@Override
	public void visitEnter(Rule rule) {
	}

	@Override
	public void visitLeave(Rule rule) {
	}

	@Override
	public void visitEnter(PolicyIDReference ref) {
	}

	@Override
	public void visitLeave(PolicyIDReference ref) {
	}

	@Override
	public void visitEnter(Policy policy) {
	}

	@Override
	public void visitLeave(Policy policy) {
	}

	@Override
	public void visitEnter(PolicySet policySet) {
	}

	@Override
	public void visitLeave(PolicySet policySet) {
	}

	@Override
	public void visitEnter(PolicySetIDReference ref) {
	}

	@Override
	public void visitLeave(PolicySetIDReference ref) {
	}

	@Override
	public void visitEnter(ObligationExpression obligation) {
	}

	@Override
	public void visitLeave(ObligationExpression obligation) {
	}

	@Override
	public void visitEnter(AdviceExpression advice) {
	}

	@Override
	public void visitLeave(AdviceExpression advice) {
	}

	@Override
	public void visitEnter(AttributeAssignmentExpression attribute) {
	}

	@Override
	public void visitLeave(AttributeAssignmentExpression attribute) {
	}

	@Override
	public void visitEnter(PolicyDefaults policyDefaults) {
	}

	@Override
	public void visitLeave(PolicyDefaults policyDefaults) {
	}

	@Override
	public void visitEnter(PolicySetDefaults policySetDefaults) {
	}

	@Override
	public void visitLeave(PolicySetDefaults policySetDefaults) {
	}

	@Override
	public void visitEnter(CombinerParameter p) {
	}

	@Override
	public void visitLeave(CombinerParameter p) {
	}
}
