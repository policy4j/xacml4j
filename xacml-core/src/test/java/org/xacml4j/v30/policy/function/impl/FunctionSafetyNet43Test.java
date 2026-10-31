package org.xacml4j.v30.policy.function.impl;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Content;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.Path;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Table B of run 037, resolved by URN through the default function provider.
 * XACML 3.0 Core OS (22 January 2013), Appendix A.3.
 * The two rows of decision C3 (35, 36) follow ruling D4 of run 061
 * (bead policy4j-the.4.59); IntegerOverflowTest has the other boundaries.
 */
@ExtendWith(MockitoExtension.class)
public class FunctionSafetyNet43Test
{
	private static final String F = "urn:oasis:names:tc:xacml:";
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();

	private EvaluationContext context;

	@BeforeEach
	public void init()
	{
		this.context = mock(EvaluationContext.class);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("higherOrderRows")
	public void higherOrderQuantifiers(String name, String functionId, String predicateId,
			BagOfValues first, BagOfValues second, boolean expected)
	{
		ValueExp result = invoke(functionId, ref(predicateId), first, second);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(expected), result);
	}

	static Stream<Arguments> higherOrderRows()
	{
		return Stream.of(
				Arguments.of("row1 any-of-all integer-equal",
						F + "1.0:function:any-of-all", F + "1.0:function:integer-equal",
						ints(1, 2), ints(1, 2), true),
				Arguments.of("row2 any-of-all published greater-than",
						F + "1.0:function:any-of-all", F + "1.0:function:integer-greater-than",
						ints(3, 5), ints(1, 2, 3, 4), true),
				Arguments.of("row3 all-of-any published greater-than",
						F + "1.0:function:all-of-any", F + "1.0:function:integer-greater-than",
						ints(3, 5), ints(1, 2, 3, 4), true));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("typedRegexpUrns")
	public void typedRegexpMatchUsesThe20UrnAndKeepsThe10Alias(String typeName)
	{
		String canonical = F + "2.0:function:" + typeName + "-regexp-match";
		String legacy = F + "1.0:function:" + typeName + "-regexp-match";
		FunctionSpec spec = require(canonical);
		assertEquals(canonical, spec.getId());
		assertEquals(legacy, spec.getLegacyId().orElseThrow());
		assertSame(spec, require(legacy));
	}

	static Stream<String> typedRegexpUrns()
	{
		return Stream.of("anyURI", "ipAddress", "dnsName", "rfc822Name", "x500Name");
	}

	@Test
	public void row6StringRegexpMatchStaysAt10()
	{
		FunctionSpec spec = require(F + "1.0:function:string-regexp-match");
		assertEquals(F + "1.0:function:string-regexp-match", spec.getId());
		assertTrue(spec.getLegacyId().isEmpty());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("stringFromUrns")
	public void stringFromFunctionsUseTheCanonicalUrn(String functionId)
	{
		assertEquals(functionId, require(functionId).getId());
	}

	static Stream<String> stringFromUrns()
	{
		return Stream.of(
				"string-from-boolean",
				"string-from-integer",
				"string-from-double",
				"string-from-time",
				"string-from-date",
				"string-from-dateTime",
				"string-from-anyURI",
				"string-from-dayTimeDuration",
				"string-from-yearMonthDuration",
				"string-from-x500Name",
				"string-from-rfc822Name",
				"string-from-ipAddress",
				"string-from-dnsName")
				.map(name -> F + "3.0:function:" + name);
	}

	@Test
	public void row10DoubleToIntegerTruncatesThroughLong()
	{
		assertEquals(XacmlTypes.INTEGER, returnType(F + "1.0:function:double-to-integer"));
		assertEquals(XacmlTypes.INTEGER.ofAny(3000000000L),
				invoke(F + "1.0:function:double-to-integer", XacmlTypes.DOUBLE.ofAny(3000000000.9d)));
	}

	@Test
	public void row11DoubleToIntegerOfFivePointNine()
	{
		assertEquals(XacmlTypes.INTEGER, returnType(F + "1.0:function:double-to-integer"));
		assertEquals(XacmlTypes.INTEGER.ofAny(5),
				invoke(F + "1.0:function:double-to-integer", XacmlTypes.DOUBLE.ofAny(5.9d)));
	}

	@Test
	public void doubleToIntegerRejectsAFiniteValueOutsideSignedLong()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:double-to-integer",
				XacmlTypes.DOUBLE.ofAny(9223372036854775808.0d));
	}

	// Decision C5, maintainer ruling 2026-10-07 (run 061 D6): NaN and the infinities have no xs:integer.
	@Test
	public void row12DoubleToIntegerOfNaNIsIndeterminate()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:double-to-integer", XacmlTypes.DOUBLE.ofAny(Double.NaN));
	}

	@Test
	public void row12DoubleToIntegerOfAnInfinityIsIndeterminate()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:double-to-integer", XacmlTypes.DOUBLE.ofAny(Double.POSITIVE_INFINITY));
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:double-to-integer", XacmlTypes.DOUBLE.ofAny(Double.NEGATIVE_INFINITY));
	}

	@Test
	public void row13IntegerToDoubleRejectsAValueThatIsNotExact()
	{
		assertEquals(XacmlTypes.DOUBLE, returnType(F + "1.0:function:integer-to-double"));
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-to-double",
				XacmlTypes.INTEGER.ofAny(9007199254740993L));
	}

	@Test
	public void row14IntegerToDoubleOfTwo()
	{
		assertEquals(XacmlTypes.DOUBLE, returnType(F + "1.0:function:integer-to-double"));
		assertEquals(XacmlTypes.DOUBLE.ofAny(2.0d),
				invoke(F + "1.0:function:integer-to-double", XacmlTypes.INTEGER.ofAny(2)));
	}

	@Test
	public void integerToDoubleKeepsAnExactWideInteger()
	{
		// 2^53 is the last integer width at which every integer is an xs:double (A.3.4).
		assertEquals(XacmlTypes.DOUBLE.ofAny(9007199254740992.0d),
				invoke(F + "1.0:function:integer-to-double",
						XacmlTypes.INTEGER.ofAny(9007199254740992L)));
	}

	@Test
	public void integerToDoubleRejectsLongMaxValue()
	{
		// (double) Long.MAX_VALUE is 2^63. Casting that back saturates (JLS 5.1.3).
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-to-double",
				XacmlTypes.INTEGER.ofAny(Long.MAX_VALUE));
	}

	@Test
	public void integerToDoubleAcceptsLongMinValue()
	{
		// -2^63 is an exact xs:double and the low end of xs:integer.
		assertEquals(XacmlTypes.DOUBLE.ofAny(-9223372036854775808.0d),
				invoke(F + "1.0:function:integer-to-double",
						XacmlTypes.INTEGER.ofAny(Long.MIN_VALUE)));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("rfc822Rows")
	public void rfc822NameMatch(String name, String pattern, String address, boolean expected)
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(expected),
				invoke(F + "1.0:function:rfc822Name-match",
						XacmlTypes.STRING.ofAny(pattern),
						XacmlTypes.RFC822NAME.ofAny(address)));
	}

	static Stream<Arguments> rfc822Rows()
	{
		return Stream.of(
				Arguments.of("row15 domain does not match a subdomain", "sun.com", "user@east.sun.com", false),
				Arguments.of("row16 domain does not match a suffix inside a label", "sun.com", "user@notsun.com", false),
				Arguments.of("row17 exact domain", "sun.com", "user@sun.com", true),
				Arguments.of("row18 leading dot matches that domain", ".east.sun.com", "user@east.sun.com", true),
				Arguments.of("leading dot matches a subdomain", ".east.sun.com", "user@dept.east.sun.com", true),
				Arguments.of("empty pattern does not match", "", "user@sun.com", false));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("regexpRows")
	public void stringRegexpMatch(String name, String pattern, String input, boolean expected)
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(expected),
				invoke(F + "1.0:function:string-regexp-match",
						XacmlTypes.STRING.ofAny(pattern),
						XacmlTypes.STRING.ofAny(input)));
	}

	static Stream<Arguments> regexpRows()
	{
		// XF 7.6.2 fn:matches is a substring match. ^ and $ bind one alternative.
		// '.' does not match a newline unless the caller passes the s flag, which
		// these functions do not.
		return Stream.of(
				Arguments.of("row19 alternation matches the second branch", "foo|bar", "xbar", true),
				Arguments.of("row20 anchors", "^foo$", "foo", true),
				Arguments.of("empty pattern matches a string", "", "x", true),
				Arguments.of("empty pattern matches the empty string", "", "", true),
				Arguments.of("empty pattern matches a newline", "", "\n", true),
				Arguments.of("anchor binds only the first branch", "^foo|bar", "xbar", true),
				Arguments.of("end anchor binds only the second branch", "foo|bar$", "foox", true),
				Arguments.of("full anchors reject a longer string", "^foo$", "xfoo", false),
				Arguments.of("dot does not match a newline", "a.c", "a\nc", false),
				Arguments.of("alternation does not match a third string", "foo|bar", "qux", false));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("typedRegexpRows")
	public void typedRegexpMatchUsesTheSameSubstringPath(String name, String functionId,
			String pattern, Expression value, boolean expected)
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(expected),
				invoke(functionId, XacmlTypes.STRING.ofAny(pattern), value));
	}

	static Stream<Arguments> typedRegexpRows()
	{
		String branch = "2.0:function:";
		return Stream.of(
				Arguments.of("anyURI branch anchor",
						F + branch + "anyURI-regexp-match",
						"^https://example.test/foo|bar",
						XacmlTypes.ANYURI.ofAny("http://example.com/xbar"), true),
				Arguments.of("anyURI empty pattern",
						F + branch + "anyURI-regexp-match",
						"",
						XacmlTypes.ANYURI.ofAny("http://example.com/xbar"), true),
				Arguments.of("anyURI full anchors reject",
						F + branch + "anyURI-regexp-match",
						"^foo$",
						XacmlTypes.ANYURI.ofAny("http://example.com/xbar"), false),
				Arguments.of("ipAddress branch anchor",
						F + branch + "ipAddress-regexp-match",
						"^10\\.0\\.0\\.1|2\\.1",
						XacmlTypes.IPADDRESS.ofAny("192.0.2.1"), true),
				Arguments.of("dnsName branch anchor",
						F + branch + "dnsName-regexp-match",
						"^not-example\\.test|com",
						XacmlTypes.DNSNAME.ofAny("example.com"), true),
				Arguments.of("rfc822Name branch anchor",
						F + branch + "rfc822Name-regexp-match",
						"^nope@example\\.test|sun",
						XacmlTypes.RFC822NAME.ofAny("user@sun.com"), true),
				Arguments.of("rfc822Name empty pattern",
						F + branch + "rfc822Name-regexp-match",
						"",
						XacmlTypes.RFC822NAME.ofAny("user@sun.com"), true),
				Arguments.of("x500Name branch anchor",
						F + branch + "x500Name-regexp-match",
						"^CN=Nobody|Corp",
						XacmlTypes.X500NAME.ofAny("cn=John Smith,o=Medico Corp,c=US"), true));
	}

	@Test
	public void row21XPathNodeCountPropagatesEvaluationFailure()
	{
		Path path = XacmlTypes.XPATH.ofAny("/root", CategoryId.SUBJECT_ACCESS);
		Content content = throwingContent();
		when(content.getType()).thenReturn(Content.Type.XML_UTF8);
		when(context.resolveEntity(path.getCategory().orElse(null)))
				.thenReturn(Optional.of(Entity.of(content)));
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "3.0:function:xpath-node-count", path);
	}

	@Test
	public void row22XPathNodeEqualPropagatesEvaluationFailure()
	{
		Path path = XacmlTypes.XPATH.ofAny("/root", CategoryId.SUBJECT_ACCESS);
		Content content = throwingContent();
		when(context.resolveContent(path.getCategory().orElse(null), path.getContentType()))
				.thenReturn(Optional.of(content));
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "3.0:function:xpath-node-equal", path, path);
	}

	@Test
	public void row23XPathNodeCountWithoutContentIsZero()
	{
		Path path = XacmlTypes.XPATH.ofAny("/root", CategoryId.SUBJECT_ACCESS);
		when(context.resolveEntity(path.getCategory().orElse(null))).thenReturn(Optional.empty());
		assertEquals(XacmlTypes.INTEGER.ofAny(0),
				invoke(F + "3.0:function:xpath-node-count", path));
	}

	@Test
	public void row24XPathNodeEqualWithoutContentIsFalse()
	{
		Path path = XacmlTypes.XPATH.ofAny("/root", CategoryId.SUBJECT_ACCESS);
		when(context.resolveContent(path.getCategory().orElse(null), path.getContentType()))
				.thenReturn(Optional.empty());
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "3.0:function:xpath-node-equal", path, path));
	}

	@Test
	public void row25XPathNodeMatchWithoutContentIsFalse()
	{
		Path path = XacmlTypes.XPATH.ofAny("/root", CategoryId.SUBJECT_ACCESS);
		when(context.resolveContent(path.getCategory().orElse(null), path.getContentType()))
				.thenReturn(Optional.empty());
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "3.0:function:xpath-node-match", path, path));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("xpathAbsentContentRows")
	public void xpathComparisonIsFalseWhenEitherContentIsAbsent(String name, String functionId,
			boolean failingExpressionFirst)
	{
		// A.3.15: either missing Content is false, even when the other XPath would fail.
		Path failing = XacmlTypes.XPATH.ofAny("/broken", CategoryId.SUBJECT_ACCESS);
		Path absent = XacmlTypes.XPATH.ofAny("/root", CategoryId.RESOURCE);
		Content content = lenientThrowingContent();
		when(context.resolveContent(failing.getCategory().orElse(null), failing.getContentType()))
				.thenReturn(Optional.of(content));
		when(context.resolveContent(absent.getCategory().orElse(null), absent.getContentType()))
				.thenReturn(Optional.empty());
		Expression first = failingExpressionFirst ? failing : absent;
		Expression second = failingExpressionFirst ? absent : failing;
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), invoke(functionId, first, second));
	}

	static Stream<Arguments> xpathAbsentContentRows()
	{
		return Stream.of(
				Arguments.of("xpath-node-equal failing expression first",
						F + "3.0:function:xpath-node-equal", true),
				Arguments.of("xpath-node-equal absent content first",
						F + "3.0:function:xpath-node-equal", false),
				Arguments.of("xpath-node-match failing expression first",
						F + "3.0:function:xpath-node-match", true),
				Arguments.of("xpath-node-match absent content first",
						F + "3.0:function:xpath-node-match", false));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("xpathBothPresentFailureRows")
	public void xpathComparisonIsIndeterminateWhenBothContentsArePresent(String functionId)
	{
		Path left = XacmlTypes.XPATH.ofAny("/broken", CategoryId.SUBJECT_ACCESS);
		Path right = XacmlTypes.XPATH.ofAny("/also-broken", CategoryId.RESOURCE);
		Content content = throwingContent();
		when(context.resolveContent(left.getCategory().orElse(null), left.getContentType()))
				.thenReturn(Optional.of(content));
		when(context.resolveContent(right.getCategory().orElse(null), right.getContentType()))
				.thenReturn(Optional.of(content));
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR, functionId, left, right);
	}

	static Stream<String> xpathBothPresentFailureRows()
	{
		return Stream.of(
				F + "3.0:function:xpath-node-equal",
				F + "3.0:function:xpath-node-match");
	}

	@Test
	public void row26XPathNodeMatchDoesNotTreatEqualAttributesAsIdentity()
	{
		XmlContent content = xml("<root><a id=\"1\"/><b id=\"1\"/></root>");
		Path left = XacmlTypes.XPATH.ofAny("/root/a", CategoryId.SUBJECT_ACCESS);
		Path right = XacmlTypes.XPATH.ofAny("/root/b", CategoryId.SUBJECT_ACCESS);
		stubContent(left, content);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "3.0:function:xpath-node-match", left, right));
	}

	@Test
	public void xpathNodeMatchIsTrueForTheSameNode()
	{
		XmlContent content = xml("<root><a id=\"1\"/></root>");
		Path path = XacmlTypes.XPATH.ofAny("/root/a", CategoryId.SUBJECT_ACCESS);
		stubContent(path, content);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "3.0:function:xpath-node-match", path, path));
	}

	@Test
	public void xpathNodeMatchIsTrueWhenTheSecondNodeIsBelowTheFirst()
	{
		XmlContent content = xml("<root><child/></root>");
		Path parent = XacmlTypes.XPATH.ofAny("/root", CategoryId.SUBJECT_ACCESS);
		Path child = XacmlTypes.XPATH.ofAny("/root/child", CategoryId.SUBJECT_ACCESS);
		stubContent(parent, content);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "3.0:function:xpath-node-match", parent, child));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("doubleRelationalRows")
	public void doubleRelational(String name, String functionId, double left, double right, boolean expected)
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(expected),
				invoke(functionId, XacmlTypes.DOUBLE.ofAny(left), XacmlTypes.DOUBLE.ofAny(right)));
	}

	static Stream<Arguments> doubleRelationalRows()
	{
		return Stream.of(
				Arguments.of("row27 NaN is not >= NaN",
						F + "1.0:function:double-greater-than-or-equal", Double.NaN, Double.NaN, false),
				Arguments.of("row28 negative zero is >= zero",
						F + "1.0:function:double-greater-than-or-equal", -0.0d, 0.0d, true),
				Arguments.of("NaN is not <= NaN",
						F + "1.0:function:double-less-than-or-equal", Double.NaN, Double.NaN, false),
				Arguments.of("negative zero is <= zero",
						F + "1.0:function:double-less-than-or-equal", -0.0d, 0.0d, true),
				Arguments.of("row29 NaN is not > NaN",
						F + "1.0:function:double-greater-than", Double.NaN, Double.NaN, false));
	}

	@Test
	public void row30DoubleEqualOfNaNIsFalse()
	{
		// XACML 3.0 A.3.1: the maintainer selected IEEE equality for task 4.6.
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:double-equal",
						XacmlTypes.DOUBLE.ofAny(Double.NaN), XacmlTypes.DOUBLE.ofAny(Double.NaN)));
	}

	@Test
	public void row31NegativeZeroEqualsZero()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:double-equal",
						XacmlTypes.DOUBLE.ofAny(-0.0d), XacmlTypes.DOUBLE.ofAny(0.0d)));
	}

	@Test
	public void row32BooleanFromStringKeepsSyntaxError()
	{
		assertIndeterminate(StatusCodeId.SYNTAX_ERROR,
				F + "3.0:function:boolean-from-string", XacmlTypes.STRING.ofAny("TRUE"));
	}

	@Test
	public void row33BooleanFromStringOne()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "3.0:function:boolean-from-string", XacmlTypes.STRING.ofAny("1")));
	}

	@Test
	public void row34BooleanFromStringCollapsesXmlSpace()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "3.0:function:boolean-from-string", XacmlTypes.STRING.ofAny(" true ")));
	}

	@Test
	public void row35IntegerAbsOfMinLongDoesNotStayNegative()
	{
		// Ruling D4 of run 061: a result outside 64 bits is Indeterminate.
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-abs", XacmlTypes.INTEGER.ofAny(Long.MIN_VALUE));
	}

	@Test
	public void row36IntegerAddDoesNotWrapToMinLong()
	{
		// Ruling D4 of run 061: a result outside 64 bits is Indeterminate.
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-add",
				XacmlTypes.INTEGER.ofAny(Long.MAX_VALUE), XacmlTypes.INTEGER.ofAny(1));
	}

	@Test
	public void row37IntegerDivideFiveByTwoIsTheIntegerTwo()
	{
		assertEquals(XacmlTypes.INTEGER, returnType(F + "1.0:function:integer-divide"));
		assertEquals(XacmlTypes.INTEGER.ofAny(2),
				invoke(F + "1.0:function:integer-divide",
						XacmlTypes.INTEGER.ofAny(5), XacmlTypes.INTEGER.ofAny(2)));
	}

	@Test
	public void row38IntegerDivideSevenByNegativeThree()
	{
		// Approved project quotient toward zero; A.3.2 mandates the integer type.
		assertEquals(XacmlTypes.INTEGER.ofAny(-2),
				invoke(F + "1.0:function:integer-divide",
						XacmlTypes.INTEGER.ofAny(7), XacmlTypes.INTEGER.ofAny(-3)));
	}

	@Test
	public void row39IntegerDivideByZeroIsIndeterminate()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-divide",
				XacmlTypes.INTEGER.ofAny(1), XacmlTypes.INTEGER.ofAny(0));
	}

	@Test
	public void row40DoubleDivideByZeroIsIndeterminate()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:double-divide",
				XacmlTypes.DOUBLE.ofAny(1.0d), XacmlTypes.DOUBLE.ofAny(0.0d));
	}

	@Test
	public void row41DoubleDivideByNegativeZeroIsIndeterminate()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:double-divide",
				XacmlTypes.DOUBLE.ofAny(1.0d), XacmlTypes.DOUBLE.ofAny(-0.0d));
	}

	@Test
	public void row42IntegerModKeepsTheSignOfTheDividend()
	{
		assertEquals(XacmlTypes.INTEGER.ofAny(-2),
				invoke(F + "1.0:function:integer-mod",
						XacmlTypes.INTEGER.ofAny(-5), XacmlTypes.INTEGER.ofAny(3)));
	}

	@Test
	public void row43IntegerModByZeroIsIndeterminate()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-mod",
				XacmlTypes.INTEGER.ofAny(5), XacmlTypes.INTEGER.ofAny(0));
	}

	@Test
	public void row44IntegerMultiply()
	{
		assertEquals(XacmlTypes.INTEGER.ofAny(24),
				invoke(F + "1.0:function:integer-multiply",
						XacmlTypes.INTEGER.ofAny(2),
						XacmlTypes.INTEGER.ofAny(3),
						XacmlTypes.INTEGER.ofAny(4)));
	}

	@Test
	public void row45DoubleMultiply()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(24.0d),
				invoke(F + "1.0:function:double-multiply",
						XacmlTypes.DOUBLE.ofAny(2.0d),
						XacmlTypes.DOUBLE.ofAny(3.0d),
						XacmlTypes.DOUBLE.ofAny(4.0d)));
	}

	@Test
	public void row46IntegerUnionDropsDuplicates()
	{
		assertEquals(ints(1, 2, 3),
				invoke(F + "1.0:function:integer-union", ints(1, 1, 2), ints(2, 3)));
	}

	@Test
	public void row47IntegerIntersection()
	{
		assertEquals(ints(2),
				invoke(F + "1.0:function:integer-intersection", ints(1, 1, 2), ints(2, 2, 3)));
	}

	@Test
	public void row48DoubleUnionCollapsesSignedZero()
	{
		// A.3.11 uses type-equal. Either zero sign is a valid representative.
		BagOfValues result = (BagOfValues) invoke(F + "1.0:function:double-union",
				XacmlTypes.DOUBLE.bagBuilder().value(0.0d).build(),
				XacmlTypes.DOUBLE.bagBuilder().value(-0.0d).build());
		assertEquals(XacmlTypes.DOUBLE, result.getBagValueType());
		assertEquals(1, result.size());
		assertTrue(result.<DoubleVal>value().get().doubleValue() == 0.0d);
	}

	@Test
	public void row49IntegerOneAndOnlyRejectsAnEmptyBag()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-one-and-only", XacmlTypes.INTEGER.emptyBag());
	}

	@Test
	public void row50IntegerOneAndOnlyRejectsTwoValues()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-one-and-only", ints(1, 1));
	}

	@Test
	public void row51IntegerOneAndOnly()
	{
		assertEquals(XacmlTypes.INTEGER.ofAny(7),
				invoke(F + "1.0:function:integer-one-and-only", ints(7)));
	}

	@Test
	public void row52IntegerUnionRejectsAStringBag()
	{
		assertIndeterminate(StatusCodeId.STATUS_PROCESSING_ERROR,
				F + "1.0:function:integer-union",
				ints(1), XacmlTypes.STRING.bagBuilder().value("a").build());
	}

	@Test
	public void row53IntegerSubset()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:integer-subset", ints(1), ints(1, 2)));
	}

	@Test
	public void row54IntegerAtLeastOneMemberOf()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:integer-at-least-one-member-of", ints(9, 2), ints(1, 2)));
	}

	@Test
	public void row55DateTimeSubtractDayTimeDuration()
	{
		assertEquals(XacmlTypes.DATETIME.ofAny("2002-03-22T07:00:00Z"),
				invoke(F + "3.0:function:dateTime-subtract-dayTimeDuration",
						XacmlTypes.DATETIME.ofAny("2002-03-22T08:00:00Z"),
						XacmlTypes.DAYTIMEDURATION.ofAny("PT1H")));
	}

	@Test
	public void row56DateTimeSubtractANegativeDayTimeDuration()
	{
		assertEquals(XacmlTypes.DATETIME.ofAny("2002-03-22T09:00:00Z"),
				invoke(F + "3.0:function:dateTime-subtract-dayTimeDuration",
						XacmlTypes.DATETIME.ofAny("2002-03-22T08:00:00Z"),
						XacmlTypes.DAYTIMEDURATION.ofAny("-PT1H")));
	}

	@Test
	public void row57X500NameMatchPublishedExample()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("O=Medico Corp,C=US"),
						XacmlTypes.X500NAME.ofAny("cn=John Smith,o=Medico Corp,c=US")));
	}

	/*
	 * Bead policy4j-the.4.44, maintainer ruling 2026-10-07 (run 061 D9, decision C8): the
	 * first name matches when its RDNs are a terminal sequence of the RDNs of the second
	 * name, each compared as x500Name-equal compares them. A string suffix is not enough.
	 */
	@Test
	public void x500NameMatchRejectsAPartOfAMultiValuedTerminalRdn()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("OU=Engineering,C=US"),
						XacmlTypes.X500NAME.ofAny("O=Acme+OU=Engineering,C=US")));
	}

	@Test
	public void x500NameMatchAcceptsAnEqualMultiValuedTerminalRdn()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("O=Acme+OU=Engineering,C=US"),
						XacmlTypes.X500NAME.ofAny("CN=John Smith,O=Acme+OU=Engineering,C=US")));
	}

	@Test
	public void x500NameMatchComparesRdnsAsX500NameEqualDoes()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("o=medico corp, c=us"),
						XacmlTypes.X500NAME.ofAny("CN=John Smith,O=Medico Corp,C=US")));
		// x500Name-equal ignores the order of the values of a multi-valued RDN.
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-equal",
						XacmlTypes.X500NAME.ofAny("OU=Engineering+O=Acme,C=US"),
						XacmlTypes.X500NAME.ofAny("O=Acme+OU=Engineering,C=US")));
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("OU=Engineering+O=Acme,C=US"),
						XacmlTypes.X500NAME.ofAny("CN=John Smith,O=Acme+OU=Engineering,C=US")));
	}

	@Test
	public void x500NameMatchKeepsTheValueEncodingThatX500NameEqualKeeps()
	{
		// #1e020041 is the BMPString "A". x500Name-equal does not equate it with the printable "A".
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:x500Name-equal",
						XacmlTypes.X500NAME.ofAny("CN=#1e020041"),
						XacmlTypes.X500NAME.ofAny("CN=A")));
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("CN=#1e020041"),
						XacmlTypes.X500NAME.ofAny("CN=A")));
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("CN=#1e020041"),
						XacmlTypes.X500NAME.ofAny("CN=#1e020041")));
	}

	@Test
	public void x500NameMatchDoesNotSplitAnRdnAtAnEscapedComma()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("CN=b,O=x"),
						XacmlTypes.X500NAME.ofAny("CN=a\\,b,O=x")));
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("O=x"),
						XacmlTypes.X500NAME.ofAny("CN=a\\,b,O=x")));
	}

	@Test
	public void x500NameMatchAgreesWithX500NameEqualOnNamesOfTheSameLength()
	{
		// For two names with the same number of RDNs, "terminal sequence" means "equal".
		String[][] pairs = {
				{"2.5.4.10=Acme,C=US", "O=Acme,C=US"},
				{"O=Acme,C=US", "O=acme , c=us"},
				{"CN=a\\,b,O=x", "CN=a\\,b,O=x"},
				{"CN=a\\,b,O=x", "CN=a,O=x"},
				{"OU=Engineering+O=Acme,C=US", "O=Acme+OU=Engineering,C=US"},
				{"OU=Engineering,C=US", "O=Acme+OU=Engineering,C=US"},
				{"CN=#1e020041", "CN=A"},
				{"", ""}};
		for (String[] pair : pairs) {
			assertEquals(
					invoke(F + "1.0:function:x500Name-equal",
							XacmlTypes.X500NAME.ofAny(pair[0]), XacmlTypes.X500NAME.ofAny(pair[1])),
					invoke(F + "1.0:function:x500Name-match",
							XacmlTypes.X500NAME.ofAny(pair[0]), XacmlTypes.X500NAME.ofAny(pair[1])),
					pair[0] + " vs " + pair[1]);
		}
	}

	@Test
	public void x500NameMatchAcceptsAnEmptyFirstName()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny(""),
						XacmlTypes.X500NAME.ofAny("O=Medico Corp,C=US")));
	}

	@Test
	public void x500NameMatchAcceptsEqualNamesAndRejectsALongerFirstName()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("O=Medico Corp,C=US"),
						XacmlTypes.X500NAME.ofAny("o=Medico Corp,c=US")));
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("CN=John Smith,O=Medico Corp,C=US"),
						XacmlTypes.X500NAME.ofAny("O=Medico Corp,C=US")));
	}

	@Test
	public void x500NameMatchRejectsADifferentRdnValueOrANonTerminalSequence()
	{
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("O=Corp,C=US"),
						XacmlTypes.X500NAME.ofAny("CN=John Smith,O=Medico Corp,C=US")));
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false),
				invoke(F + "1.0:function:x500Name-match",
						XacmlTypes.X500NAME.ofAny("CN=John Smith,O=Medico Corp"),
						XacmlTypes.X500NAME.ofAny("CN=John Smith,O=Medico Corp,C=US")));
	}

	// Decision C7, maintainer ruling 2026-10-07 (run 061 D8): the 1.0 ipAddress set functions stay as extensions.
	@Test
	public void ipAddressSetFunctionsStayRegisteredAsExtensions()
	{
		for (String name : new String[] {"intersection", "union", "subset",
				"at-least-one-member-of", "set-equals"}) {
			assertTrue(FUNCTIONS.getFunction(F + "1.0:function:ipAddress-" + name).isPresent(), name);
		}
	}

	@Test
	public void row58LowerCaseDoesNotUseTheDefaultLocale()
	{
		Locale previous = Locale.getDefault();
		Locale.setDefault(Locale.forLanguageTag("tr"));
		try {
			assertEquals(XacmlTypes.STRING.ofAny("i"),
					invoke(F + "1.0:function:string-normalize-to-lower-case",
							XacmlTypes.STRING.ofAny("I")));
		} finally {
			Locale.setDefault(previous);
		}
	}

	@Test
	public void row59NormalizeSpaceKeepsAVerticalTab()
	{
		assertEquals(XacmlTypes.STRING.ofAny("\u000Ba"),
				invoke(F + "1.0:function:string-normalize-space",
						XacmlTypes.STRING.ofAny("\u000Ba")));
	}

	@Test
	public void normalizeSpaceStripsXmlSpaceAndKeepsInternalSpaces()
	{
		assertEquals(XacmlTypes.STRING.ofAny("a  b"),
				invoke(F + "1.0:function:string-normalize-space",
						XacmlTypes.STRING.ofAny(" a  b ")));
	}

	@Test
	public void floorOfNaNStaysNaN()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(Double.NaN),
				invoke(F + "1.0:function:floor", XacmlTypes.DOUBLE.ofAny(Double.NaN)));
	}

	/*
	 * Decision C4, maintainer ruling 2026-10-07 (run 061 D5): round follows IEEE 754
	 * round-to-nearest. NaN stays NaN, an infinity stays that infinity, and a tie goes
	 * to the even integer.
	 */
	@Test
	public void row60RoundOfNaNStaysNaN()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(Double.NaN),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(Double.NaN)));
	}

	@Test
	public void row60RoundOfAnInfinityStaysThatInfinity()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(Double.POSITIVE_INFINITY),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(Double.POSITIVE_INFINITY)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(Double.NEGATIVE_INFINITY),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(Double.NEGATIVE_INFINITY)));
	}

	@Test
	public void row60RoundSendsATieToTheEvenInteger()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(0.0d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(0.5d)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(2.0d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(1.5d)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(2.0d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(2.5d)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(-2.0d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(-2.5d)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(-2.0d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(-1.5d)));
		// The tie between -1 and 0 goes to zero; IEEE 754 keeps the sign.
		assertEquals(Double.doubleToRawLongBits(-0.0d), Double.doubleToRawLongBits(
				((org.xacml4j.v30.types.DoubleVal) invoke(F + "1.0:function:round",
						XacmlTypes.DOUBLE.ofAny(-0.5d))).get()));
	}

	@Test
	public void row60RoundKeepsOrdinaryNearestRounding()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(3.0d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(2.6d)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(-3.0d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(-2.6d)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(9.007199254740993E15d),
				invoke(F + "1.0:function:round", XacmlTypes.DOUBLE.ofAny(9.007199254740993E15d)));
	}

	@Test
	public void floorOfAnInfinityStaysThatInfinity()
	{
		assertEquals(XacmlTypes.DOUBLE.ofAny(Double.POSITIVE_INFINITY),
				invoke(F + "1.0:function:floor", XacmlTypes.DOUBLE.ofAny(Double.POSITIVE_INFINITY)));
		assertEquals(XacmlTypes.DOUBLE.ofAny(Double.NEGATIVE_INFINITY),
				invoke(F + "1.0:function:floor", XacmlTypes.DOUBLE.ofAny(Double.NEGATIVE_INFINITY)));
	}

	private ValueExp invoke(String functionId, Expression... arguments)
	{
		return require(functionId).invoke(context, arguments);
	}

	private static FunctionSpec require(String functionId)
	{
		return FUNCTIONS.getFunction(functionId)
				.orElseThrow(() -> new AssertionError("missing function " + functionId));
	}

	private static FunctionReference ref(String functionId)
	{
		return new FunctionReference(require(functionId));
	}

	private static ValueType returnType(String functionId)
	{
		return require(functionId).resolveReturnType(java.util.List.of()).getValueType();
	}

	private void assertIndeterminate(StatusCodeId code, String functionId, Expression... arguments)
	{
		EvaluationException error = assertThrows(EvaluationException.class,
				() -> invoke(functionId, arguments));
		assertEquals(code, error.getStatus().getStatusCode().getValue());
	}

	private static BagOfValues ints(int... values)
	{
		BagOfValues.Builder builder = XacmlTypes.INTEGER.bagBuilder();
		for (int value : values) {
			builder.value(value);
		}
		return builder.build();
	}

	private void stubContent(Path path, Content content)
	{
		when(context.resolveContent(path.getCategory().orElse(null), path.getContentType()))
				.thenReturn(Optional.of(content));
	}

	private static XmlContent xml(String document)
	{
		return XmlContent.of(XmlContent.fromString(document));
	}

	private static Content throwingContent()
	{
		Content content = mock(Content.class);
		when(content.evaluateToNodeSet(anyString())).thenThrow(xpathFailure());
		return content;
	}

	private static Content lenientThrowingContent()
	{
		Content content = mock(Content.class);
		// The absence check must return before this evaluation. lenient because
		// a correct implementation never calls evaluateToNodeSet here.
		lenient().when(content.evaluateToNodeSet(anyString())).thenThrow(xpathFailure());
		return content;
	}

	private static EvaluationException xpathFailure()
	{
		return new EvaluationException(
				org.xacml4j.v30.Status.processingError().message("xpath evaluation failed").build(),
				"xpath evaluation failed");
	}
}
