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


import com.google.common.base.MoreObjects;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Objects;

/**
 * A base class for XACML quantified expressions
 *
 * @{@see <a hre="http://docs.oasis-open.org/xacml/xacml-3.0-related-entities/v1.0/csprd01/xacml-3.0-related-entities-v1.0-csprd01.html"/>
 * @author Giedrius Trumpickas
 */
public abstract class QuantifiedExpression implements Expression
{
    protected final static Value TRUE = XacmlTypes.BOOLEAN.of(true);
    protected final static Value FALSE = XacmlTypes.BOOLEAN.of(true);

    private Expression domain;
    private Expression iterant;
    private String variableId;

    private int hashCode = 0;

    protected QuantifiedExpression(Expression domain, Expression iterant, String variableId){
        this.domain = Objects.requireNonNull(domain, "domain");
        this.iterant = Objects.requireNonNull(iterant, "iterant");
        this.variableId = Objects.requireNonNull(variableId, "variableId");
    }

    protected final BagOfValues getDomain(EvaluationContext context){
        return domain.evaluate(context);
    }

    protected final Value evaluateIterant(
		    Value v,
		    EvaluationContext context){
        context.setVariableEvaluationResult(variableId, v);
        return iterant.evaluate(context);
    }

    /**
     * Gets domain expression for this quantified expression
     *
     * @return a domain expression
     */
    public final Expression getDomain(){
        return domain;
    }

    /**
     * Gets iterant expression for this quantified expression
     *
     * @return a iterant expression
     */
    public final Expression getIterant(){
        return iterant;
    }

    /**
     * Gets variable identifier for this expression
     *
     * @return a variable identifier
     */
    public final String getVariableId(){
        return variableId;
    }

    @Override
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("domain", domain)
                .add("iterant", iterant)
                .add("variableId", variableId)
                .toString();
    }

    @Override
    public int hashCode(){
        if(hashCode == 0){
            this.hashCode = Objects.hash(domain, iterant, variableId);;
        }
        return hashCode;
    }
}
