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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;

/**
 * XACML 3.0 A.3.1, A.3.3 and A.3.9 require the original URI spelling.
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 */
public class AnyURILexicalTest
{
    private static final FunctionProvider FUNCTIONS =
            FunctionProviderBuilder.builder().withDefaultFunctions().build();

    @ParameterizedTest
    @ValueSource(strings = {"http://example.com/a/../b", "http://example.com/./a",
            "HTTP://Example.COM/a", "http://example.com/%2f"})
    public void factoriesAndUriGetterKeepOriginalSpelling(String lexical)
    {
        AnyURI value = XacmlTypes.ANYURI.ofAny(lexical);
        assertEquals(lexical, value.get().toString());
        assertEquals(lexical, value.toXacmlString());
        assertEquals(lexical, value.toStringExp().get());
        TypeToString converter = TypeToString.forType(XacmlTypes.ANYURI).orElseThrow();
        assertEquals(lexical, converter.toString(converter.fromString(lexical)));
        assertEquals(value, XacmlTypes.ANYURI.ofAny(URI.create(lexical)));
        assertEquals(value.hashCode(), XacmlTypes.ANYURI.ofAny(URI.create(lexical)).hashCode());
    }

    @ParameterizedTest
    @MethodSource("lexicalInputs")
    public void stringConversionKeepsDotSegmentsAndCase(String lexical, boolean validate) throws Exception
    {
        AnyURI value = XacmlTypes.ANYURI.ofAny(lexical);
        assertEquals(StringVal.of(lexical), call("3.0", "string-from-anyURI", value)
                .evaluate(ModelFixtures.context(validate)));
        assertEquals(value, call("3.0", "anyURI-from-string", StringVal.of(lexical))
                .evaluate(ModelFixtures.context(validate)));
    }

    @ParameterizedTest
    @CsvSource({"ends-with,/../b,http://example.com/a/../b,true",
            "contains,a/../b,http://example.com/a/../b,true",
            "starts-with,HTTP://Example.COM,HTTP://Example.COM/a,true",
            "starts-with,http://example.com,HTTP://Example.COM/a,false",
            "contains,./a,http://example.com/./a,true",
            "contains,missing,http://example.com/a/../b,false"})
    public void uriSearchUsesOriginalSpelling(String operation, String needle, String lexical,
            boolean expected) throws Exception
    {
        for (boolean validate : new boolean[]{false, true}) {
            assertEquals(BooleanVal.of(expected), call("3.0", "anyURI-" + operation,
                    StringVal.of(needle), XacmlTypes.ANYURI.ofAny(lexical))
                    .evaluate(ModelFixtures.context(validate)));
        }
    }

    @ParameterizedTest
    @MethodSource("lexicalInputs")
    public void uriSubstringReturnsOriginalSpelling(String lexical, boolean validate) throws Exception
    {
        assertEquals(StringVal.of(lexical), call("3.0", "anyURI-substring",
                XacmlTypes.ANYURI.ofAny(lexical), IntegerVal.of(0), IntegerVal.of(-1))
                .evaluate(ModelFixtures.context(validate)));
    }

    @ParameterizedTest
    @CsvSource({"http://example.com/a/../b,http://example.com/b,false",
            "HTTP://Example.COM/a,http://example.com/a,false",
            "http://example.com/%2f,http://example.com/%2F,false",
            "http://example.com/a/../b,http://example.com/a/../b,true"})
    public void anyUriEqualUsesCodepointEquality(String first, String second, boolean expected) throws Exception
    {
        for (boolean validate : new boolean[]{false, true}) {
            assertEquals(BooleanVal.of(expected), call("1.0", "anyURI-equal",
                    XacmlTypes.ANYURI.ofAny(first), XacmlTypes.ANYURI.ofAny(second))
                    .evaluate(ModelFixtures.context(validate)));
        }
    }

    @ParameterizedTest
    @CsvSource({"http://example.com/a/../b,http://example.com/b",
            "HTTP://Example.COM/a,http://example.com/a",
            "http://example.com/%2f,http://example.com/%2F"})
    public void lexicalEqualityAndHashingKeepDistinctUriSpellings(String first, String second)
    {
        AnyURI a = XacmlTypes.ANYURI.ofAny(first);
        AnyURI b = XacmlTypes.ANYURI.ofAny(second);
        assertNotEquals(a, b);
        assertNotEquals(b, a);
        assertEquals(2, new HashSet<>(List.of(a, b)).size());
        assertEquals(2, XacmlTypes.ANYURI.bagBuilder().attribute(a, b).build().size());
    }

    @Test
    public void explicitNormalizationIsAnOptInOperation()
    {
        AnyURI value = XacmlTypes.ANYURI.ofAny(URI.create("http://example.com/a/../b"));
        assertEquals("http://example.com/b", value.normalize().toXacmlString());
        assertEquals("http://example.com/a/../b", value.toXacmlString());
    }

    private static Stream<Arguments> lexicalInputs()
    {
        return Stream.of("http://example.com/a/../b", "http://example.com/./a",
                "HTTP://Example.COM/a", "http://example.com/%2f")
                .flatMap(lexical -> Stream.of(Arguments.of(lexical, false), Arguments.of(lexical, true)));
    }

    private static Apply call(String version, String name, Expression... arguments)
    {
        String id = "urn:oasis:names:tc:xacml:" + version + ":function:" + name;
        var spec = FUNCTIONS.getFunction(id).orElseThrow();
        spec.validateParametersAndThrow(Arrays.asList(arguments));
        return Apply.builder(spec).param(arguments).build();
    }
}
