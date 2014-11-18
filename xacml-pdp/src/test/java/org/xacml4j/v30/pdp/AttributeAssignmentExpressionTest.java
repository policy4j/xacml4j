package org.xacml4j.v30.pdp;


import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createStrictMock;
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
