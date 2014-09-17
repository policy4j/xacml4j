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

import org.xacml4j.v30.PolicyVisitor;

import java.lang.annotation.*;

public class PolicyVisitorSupport implements
        AdviceExpression.Visitor,
        ObligationExpression.Visitor,
        Target.Visitor,
        Condition.Visitor,
        Match.Visitor,
        MatchAllOf.Visitor,
        MatchAnyOf.Visitor,
        Policy.Visitor,
        PolicySet.Visitor,
        Rule.Visitor,
        PolicyIDReference.Visitor,
        PolicySetIDReference.Visitor,
        PolicyDefaults.Visitor,
        PolicySetDefaults.Visitor,
        CombinerParameter.Visitor,
        VariableDefinition.Visitor
{
    @Override
    public void visitEnter(AdviceExpression exp) {

    }

    @Override
    public void visitLeave(AdviceExpression exp) {

    }

    @Override
    public void visitEnter(ObligationExpression exp) {

    }

    @Override
    public void visitLeave(ObligationExpression exp) {

    }

    @Override
    public void visitEnter(Target t) {

    }

    @Override
    public void visitLeave(Target t) {

    }

    @Override
    public void visitEnter(Condition c) {

    }

    @Override
    public void visitLeave(Condition c) {

    }

    @Override
    public void visitEnter(Match m) {

    }

    @Override
    public void visitLeave(Match m) {

    }

    @Override
    public void visitEnter(MatchAllOf m) {

    }

    @Override
    public void visitLeave(MatchAllOf m) {

    }

    @Override
    public void visitEnter(MatchAnyOf m) {

    }

    @Override
    public void visitLeave(MatchAnyOf m) {

    }

    @Override
    public void visitEnter(Policy p) {

    }

    @Override
    public void visitLeave(Policy p) {

    }

    @Override
    public void visitEnter(PolicySet ps) {

    }

    @Override
    public void visitLeave(PolicySet ps) {

    }

    @Override
    public void visitEnter(Rule r) {

    }

    @Override
    public void visitLeave(Rule r) {

    }

    @Override
    public void visitEnter(PolicyIDReference r) {

    }

    @Override
    public void visitLeave(PolicyIDReference r) {

    }

    @Override
    public void visitEnter(PolicySetIDReference r) {

    }

    @Override
    public void visitLeave(PolicySetIDReference r) {

    }

    @Override
    public void visitEnter(PolicyDefaults d) {

    }

    @Override
    public void visitLeave(PolicyDefaults d) {

    }

    @Override
    public void visitEnter(PolicySetDefaults ps) {

    }

    @Override
    public void visitLeave(PolicySetDefaults ps) {

    }

    @Override
    public void visitEnter(CombinerParameter p) {

    }

    @Override
    public void visitLeave(CombinerParameter p) {

    }

    @Override
    public void visitEnter(VariableDefinition v) {

    }

    @Override
    public void visitLeave(VariableDefinition v) {

    }
}
