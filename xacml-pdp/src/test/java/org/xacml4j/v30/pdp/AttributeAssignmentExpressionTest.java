package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J PDP related classes
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


import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

public class AttributeAssignmentExpressionTest
{
    private EvaluationContext context;
    private IMocksControl c;

    @Before
    public void init(){
        this.c = createControl();
        this.context = c.createMock(EvaluationContext.class);
    }

    @Test
    public void testAttributeAssingmentExpressionAttributeDesignator() throws Exception{
        AttributeDesignator desig = AttributeDesignator
                .builder()
                .attributeId("testId1")
                .category(Categories.RESOURCE)
                .dataType(XacmlTypes.STRING)
                .build();
        AttributeAssignmentExpression exp = AttributeAssignmentExpression
                .builder("testId")
                .category(Categories.RESOURCE)
                .expression(desig)
                .build();
        c.replay();
        expect(context.resolve(desig.getReferenceKey())).andReturn(StringExp.of("aaaaa").toBag());
        ValueExpression value = exp.evaluate(context);
        c.verify();
    }
}
