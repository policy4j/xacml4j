package org.xacml4j.v30.types;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;

public class BooleanLexicalTest
{
    // XML Schema Part 2 sections 3.2.2 and 4.3.6:
    // https://www.w3.org/TR/xmlschema-2/#boolean and #rf-whiteSpace.
    private static Stream<Arguments> legalForms()
    {
        return Stream.of(
                Arguments.of("true", true), Arguments.of("false", false),
                Arguments.of("1", true), Arguments.of("0", false),
                Arguments.of(" \t\r\ntrue \t\r\n", true),
                Arguments.of(" \t\r\nfalse \t\r\n", false),
                Arguments.of(" \t\r\n1 \t\r\n", true),
                Arguments.of(" \t\r\n0 \t\r\n", false));
    }

    private static List<Function<String, BooleanVal>> parsers()
    {
        return List.of(lexical -> XacmlTypes.BOOLEAN.ofAny(lexical),
                lexical -> XacmlTypes.BOOLEAN.ofAny(StringVal.of(lexical)),
                lexical -> (BooleanVal) TypeToString.forType(XacmlTypes.BOOLEAN).orElseThrow().fromString(lexical));
    }

    @ParameterizedTest
    @MethodSource("legalForms")
    public void factoryAndStringCapabilityAcceptOnlyTheFourForms(String lexical, boolean expected)
    {
        BooleanVal canonical = BooleanVal.of(expected);
        for (Function<String, BooleanVal> parser : parsers()) {
            BooleanVal actual = parser.apply(lexical);
            assertEquals(expected, actual.get());
            assertEquals(canonical, actual);
            assertEquals(canonical.hashCode(), actual.hashCode());
            assertEquals(expected ? "true" : "false", actual.toXacmlString());
            assertEquals(actual, parser.apply(actual.toXacmlString()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "TRUE", "True", "FALSE", "False", "", " \t\n\r ", "2", "01",
            "+1", "0.0", "tr ue", "tru\te", "true false", "\u000btrue", "true\f", "\u00a0true", "\u2003true"})
    public void invalidLexicalFormsAreSyntaxErrors(String lexical)
    {
        for (Function<String, BooleanVal> parser : parsers()) {
            SyntaxException error = assertThrows(SyntaxException.class, () -> parser.apply(lexical));
            assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
        }
    }

    @Test
    public void nativeBooleansRemainValidAndOtherObjectsRemainInvalid()
    {
        assertEquals(BooleanVal.of(true), XacmlTypes.BOOLEAN.ofAny(Boolean.TRUE));
        assertEquals(BooleanVal.of(false), XacmlTypes.BOOLEAN.ofAny(Boolean.FALSE));
        for (Object invalid : new Object[]{null, 1, 0}) {
            SyntaxException error = assertThrows(SyntaxException.class, () -> XacmlTypes.BOOLEAN.ofAny(invalid));
            assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
        }
    }
}
