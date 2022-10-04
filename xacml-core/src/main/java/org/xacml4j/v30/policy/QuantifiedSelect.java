package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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


import org.xacml4j.v30.*;


/**
 * An implementation of XACML Quantified Select expression
 *
 * @author Giedrius Trumpickas
 */
public final class QuantifiedSelect extends QuantifiedExpression
{
    public QuantifiedSelect(Expression domain,
                            Expression iterant,
                            String variableId) {
        super(domain, iterant, variableId);
    }

    @Override
    public ValueTypeInfo getEvaluatesTo() {
        return getDomain()
                .getEvaluatesTo();
    }

    @Override
    public Expression evaluate(final EvaluationContext context)
            throws EvaluationException
    {
        BagOfValues bag = getDomain(context);
        BagOfValues.Builder b = bag
                .getEvaluatesTo()
                .builder();
        for(Value v : bag.values()){
            if(evaluateIterant(v, context).equals(TRUE)){
                b.attribute(v);
            }
        }
        return b.build();
    }

    @Override
    public void accept(ExpressionVisitor v) {

    }
}


