package org.xacml4j.v30.marshal.jaxb;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2015 Xacml4J.org
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


import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.Apply;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.policy.combine.DenyOverridesRuleCombiningAlgorithm;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class Xacml30AttributeValueMarshalingTest {
    private final static FunctionProvider Funcs =
        FunctionProviderBuilder.builder()
            .defaultFunctions()
            .build();

    @Parameterized.Parameters(name = "#{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"integer", "urn:oasis:names:tc:xacml:1.0:function:integer-equal", IntegerExp.of(0)},
            {"string", "urn:oasis:names:tc:xacml:1.0:function:string-equal", StringExp.of("a")},
            {"anyURI", "urn:oasis:names:tc:xacml:1.0:function:anyURI-equal", AnyURIExp.of("about:blank")},
        });
    }

    private String name;
    private String functionId;
    private Expression expression;

    public Xacml30AttributeValueMarshalingTest(String name, String functionId, Expression expression) {
        this.name = name;
        this.functionId = functionId;
        this.expression = expression;
    }

    @org.junit.Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAttributeValueMarshaling() throws IOException {
        // arrange
        Policy policy = createPolicy();

        // act
        new Xacml30PolicyMarshaller().marshal(policy, new StringWriter());

        // assert
        // should not throw
    }

    private Policy createPolicy() {
        Rule rule = Rule.builder("rule", Effect.DENY)
            .condition(
                Apply.builder(Funcs.getFunction(functionId))
                    .param(expression)
                    .param(expression)
                    .build()
            )
            .build();

        return Policy.builder("policy")
            .combiningAlgorithm(new DenyOverridesRuleCombiningAlgorithm())
            .rule(rule)
            .build();
    }
}
