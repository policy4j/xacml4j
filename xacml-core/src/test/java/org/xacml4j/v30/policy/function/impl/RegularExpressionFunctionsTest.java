package org.xacml4j.v30.policy.function.impl;

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

import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.AnyURI;
import org.xacml4j.v30.types.RFC822Name;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;


@ExtendWith(MockitoExtension.class)
public class RegularExpressionFunctionsTest
{
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testFunctionIfImplemented() throws Exception {
		FunctionProvider f = FunctionProviderBuilder.builder()
		                                            .withDefaultFunctions()
		                                            .build();
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match"), notNullValue());
		assertThat(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match"), notNullValue());
	}

	@Test
	public void testXacmlRegExpToJERegExpWithCharacterSubtraction() {
		// XF 7.6.2 is a substring match: no ".*" padding and no empty-pattern rewrite.
		assertThat(RegExpFunctions.covertXacmlToJavaSyntax("[0-9]{3}-[0-9]{3}-[0-9]{4}"),
				equalTo("[0-9]{3}-[0-9]{3}-[0-9]{4}"));
		assertThat(RegExpFunctions.covertXacmlToJavaSyntax("[a-z-[aeiou]]"),
				equalTo("[a-z&&[^aeiou]]"));
		assertThat(RegExpFunctions.covertXacmlToJavaSyntax(""), equalTo(""));
	}

	@Test
	public void testXPathDollarAnchorMatchesOnlyTheEndOfInput() {
		StringVal regexp = XacmlTypes.STRING.ofAny("^foo$");
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp, XacmlTypes.STRING.ofAny("foo\n")),
				equalTo(XacmlTypes.BOOLEAN.ofAny(false)));
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp, XacmlTypes.STRING.ofAny("foo")),
				equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testXPathRegexEscapedDollarAndCharacterClassRemainLiteral() {
		assertThat(RegExpFunctions.covertXacmlToJavaSyntax("a\\$"), equalTo("a\\$"));
		assertThat(RegExpFunctions.covertXacmlToJavaSyntax("[$]"), equalTo("[$]"));
		assertThat(RegExpFunctions.stringRegexpMatch(context, XacmlTypes.STRING.ofAny("a\\$"),
				XacmlTypes.STRING.ofAny("a$")), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		assertThat(RegExpFunctions.stringRegexpMatch(context, XacmlTypes.STRING.ofAny("[$]"),
				XacmlTypes.STRING.ofAny("$")), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testXPathCaretAnchorMatchesOnlyTheStartOfInput() {
		StringVal regexp = XacmlTypes.STRING.ofAny("^foo");
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp, XacmlTypes.STRING.ofAny("foo\n")),
				equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp, XacmlTypes.STRING.ofAny("\nfoo")),
				equalTo(XacmlTypes.BOOLEAN.ofAny(false)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testTypedRegexpMatchVariantsUseTheSharedAnchoredMatcher() {
		StringVal regexp = XacmlTypes.STRING.ofAny(".*$");
		assertThat(RegExpFunctions.anyURIRegexpMatch(context, regexp,
				XacmlTypes.ANYURI.ofAny("https://example.com/a")), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		assertThat(RegExpFunctions.ipAddressRegexpMatch(context, regexp,
				XacmlTypes.IPADDRESS.ofAny("192.0.2.1")), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		assertThat(RegExpFunctions.dnsNameRegexpMatch(context, regexp,
				XacmlTypes.DNSNAME.ofAny("example.com")), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		assertThat(RegExpFunctions.rfc822NameRegexpMatch(context, regexp,
				XacmlTypes.RFC822NAME.ofAny("alice@example.com")), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		assertThat(RegExpFunctions.x500NameRegexpMatch(context, regexp,
				XacmlTypes.X500NAME.ofAny("CN=Alice")), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testXacmlRegExpWithSpaceBugTrimming() {
		StringVal regexp1 = XacmlTypes.STRING.ofAny("   This  is n*o*t* *IT!  ");
		StringVal regexp2 = XacmlTypes.STRING.ofAny("  *This .*is not IT! *");
		StringVal input1 = XacmlTypes.STRING.ofAny("   This  is not IT!  ");
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp1, input1), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);


		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp2, input1), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testRegExpMatchFromIIC168ConformanceTest() {
		StringVal regexp1 = XacmlTypes.STRING.ofAny("   This  is n*o*t* *IT!  ");
		StringVal input1 = XacmlTypes.STRING.ofAny("   This  is IT!  ");
		StringVal input2 = XacmlTypes.STRING.ofAny("   This  is not IT!  ");
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp1, input1), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);


		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp1, input2), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testStringRegExpMatch() throws EvaluationException {
		StringVal regexp = XacmlTypes.STRING.ofAny("S*,Value");
		StringVal input = XacmlTypes.STRING.ofAny("Some,Value");
		assertThat(RegExpFunctions.stringRegexpMatch(context, regexp, input), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException {
		StringVal regexp = XacmlTypes.STRING.ofAny("http://www.test.org/public/*");
		AnyURI input = XacmlTypes.ANYURI.ofAny("http://www.test.org/public/test/a");
		assertThat(RegExpFunctions.anyURIRegexpMatch(context, regexp, input), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);

	}

	@Test
	public void testRFC822NameRegExpMatch() throws EvaluationException{
		StringVal regexp = XacmlTypes.STRING.ofAny(".*@example.com");
		RFC822Name input = XacmlTypes.RFC822NAME.ofAny("test@example.com");
		assertThat(RegExpFunctions.rfc822NameRegexpMatch(context, regexp, input), equalTo(XacmlTypes.BOOLEAN.ofAny(true)));
		verifyNoMoreInteractions(context);
	}
}
