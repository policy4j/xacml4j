package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Policy Unit Test Support
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.AttributesType;
import org.oasis.xacml.v30.jaxb.ContentType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.PolicyIdentifierListType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.oasis.xacml.v30.jaxb.ResultType;
import org.oasis.xacml.v30.jaxb.StatusType;
import org.oasis.xacml.v30.jaxb.AttributeAssignmentType;
import org.oasis.xacml.v30.jaxb.ObligationType;
import org.oasis.xacml.v30.jaxb.ObligationsType;
import org.oasis.xacml.v30.jaxb.AdviceType;
import org.oasis.xacml.v30.jaxb.AssociatedAdviceType;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.xml.JAXBUtils;
import org.xacml4j.v30.xml.Xacml30RequestContextUnmarshaller;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;
import org.w3c.dom.Node;

import com.google.common.base.Supplier;
import com.google.common.io.Closeables;


public class Xacml30TestUtility
{
	private static final JAXBContext context = JAXBUtils.getInstance();
	private static final Xacml30RequestContextUnmarshaller requestUnmarshaller = new Xacml30RequestContextUnmarshaller();

	/** Private constructor for utility class */
	private Xacml30TestUtility() {}

	public static void assertResponse(ResponseType a, ResponseType b) {
		assertEquals(a.getResult().size(), b.getResult().size());
		List<ResultType> ar = a.getResult();
		List<ResultType> br = b.getResult();
		for(int i = 0; i < ar.size(); i++){
			assertResults(ar.get(i), br.get(i));
		}
	}

	public static void assertResults(ResultType a, ResultType b)
	{
		assertEquals(a.getDecision(), b.getDecision());
		assertPolicyIdentifierList(a.getPolicyIdentifierList(), b.getPolicyIdentifierList());
		assertAttributesType(a.getAttributes(), b.getAttributes());
		assertStatus(a.getStatus(), b.getStatus());
		assertObligations(a.getObligations(), b.getObligations());
		assertAssociatedAdvice(a.getAssociatedAdvice(), b.getAssociatedAdvice());
	}

	public static void assertAttributesType(List<AttributesType> a, List<AttributesType> b){
		assertTrue(sameMultiset(a, b, Xacml30TestUtility::sameAttributes), "Included categories differ");
	}

	public static void assertAttributeType(List<AttributeType> a, List<AttributeType> b){
		assertTrue(sameMultiset(a, b, Xacml30TestUtility::sameAttribute), "Included attributes differ");
	}

	public static void assertAttributeValues(List<AttributeValueType> a, List<AttributeValueType> b){
		assertTrue(sameMultiset(a, b, Xacml30TestUtility::sameAttributeValue), "Attribute values differ");
	}

	private static boolean sameAttributes(AttributesType a, AttributesType b)
	{
		return Objects.equals(a.getCategory(), b.getCategory())
				&& Objects.equals(a.getId(), b.getId())
				&& sameMultiset(a.getAttribute(), b.getAttribute(), Xacml30TestUtility::sameAttribute)
				&& sameContent(a.getContent(), b.getContent());
	}

	private static boolean sameContent(ContentType a, ContentType b)
	{
		if (a == null || b == null) {
			return a == b;
		}
		return Objects.equals(a.getType(), b.getType()) && sameContent(a.getContent(), b.getContent());
	}

	private static boolean sameAttribute(AttributeType a, AttributeType b)
	{
		return Objects.equals(a.getAttributeId(), b.getAttributeId())
				&& Objects.equals(a.getIssuer(), b.getIssuer())
				&& a.isIncludeInResult() == b.isIncludeInResult()
				&& sameMultiset(a.getAttributeValue(), b.getAttributeValue(), Xacml30TestUtility::sameAttributeValue);
	}

	private static boolean sameAttributeValue(AttributeValueType a, AttributeValueType b)
	{
		return Objects.equals(a.getDataType(), b.getDataType())
				&& Objects.equals(a.getOtherAttributes(), b.getOtherAttributes())
				&& sameValueContent(a.getDataType(), a.getContent(), b.getContent());
	}

	static boolean sameValueContent(String dataType, List<?> a, List<?> b)
	{
		if (a.stream().allMatch(String.class::isInstance) && b.stream().allMatch(String.class::isInstance)) {
			String expected = a.stream().map(String.class::cast).collect(java.util.stream.Collectors.joining());
			String actual = b.stream().map(String.class::cast).collect(java.util.stream.Collectors.joining());
			ValueType type = XacmlTypes.getType(dataType).orElse(null);
			if (type == null) {
				// Unknown extension types retain strict lexical comparison.
				return expected.equals(actual);
			}
			try {
				return Objects.equals(parseValue(type, expected), parseValue(type, actual));
			} catch (RuntimeException invalid) {
				throw new AssertionError("Invalid lexical attribute value for " + dataType, invalid);
			}
		}
		return sameContent(a, b);
	}

	private static Object parseValue(ValueType type, String lexical)
	{
		if (type.equals(XacmlTypes.BOOLEAN)) {
			// XML Schema boolean has four lexical forms; Boolean.parseBoolean does not.
			return switch (lexical.trim()) {
			case "true", "1" -> type.ofAny(Boolean.TRUE);
			case "false", "0" -> type.ofAny(Boolean.FALSE);
			default -> throw new IllegalArgumentException("Invalid XML Schema boolean");
			};
		}
		return type.ofAny(lexical);
	}

	static boolean sameContent(List<?> a, List<?> b)
	{
		if (a.size() != b.size()) {
			return false;
		}
		for (int i = 0; i < a.size(); i++) {
			Object expected = a.get(i);
			Object actual = b.get(i);
			if (expected instanceof Node && actual instanceof Node) {
				if (!((Node) expected).isEqualNode((Node) actual)) {
					return false;
				}
			} else if (!Objects.equals(expected, actual)) {
				return false;
			}
		}
		return true;
	}

	public static void assertPolicyIdentifierList(PolicyIdentifierListType a, PolicyIdentifierListType b){
		if(a == null && b == null){
			return;
		}
		if(a == null){
			a = new PolicyIdentifierListType();
		}
		if(b == null){
			b = new PolicyIdentifierListType();
		}
		assertEquals(a.getPolicyIdReferenceOrPolicySetIdReference().size(),
		             b.getPolicyIdReferenceOrPolicySetIdReference().size());
		for(int i = 0; i < a.getPolicyIdReferenceOrPolicySetIdReference().size(); i++){
			IdReferenceType ia = a.getPolicyIdReferenceOrPolicySetIdReference().get(i).getValue();
			IdReferenceType ib = b.getPolicyIdReferenceOrPolicySetIdReference().get(i).getValue();
			assertEquals(ia.getValue(),ib.getValue());
			assertEquals(ia.getEarliestVersion(),ib.getEarliestVersion());
			assertEquals(ia.getVersion(),ib.getVersion());
			assertEquals(ia.getLatestVersion(),ib.getLatestVersion());
		}
	}

	public static void assertStatus(StatusType a, StatusType b)
	{
		assertEquals(a.getStatusCode().getValue(), b.getStatusCode().getValue());
	}

	public static void assertObligations(ObligationsType a, ObligationsType b)
	{
		if(a == null && b == null){
			return;
		}
		if(a == null){
			a = new ObligationsType();
		}
		if(b == null){
			b = new ObligationsType();
		}
		List<ObligationType> oa = a.getObligation();
		List<ObligationType> ob = b.getObligation();
		assertTrue(sameMultiset(oa, ob, Xacml30TestUtility::sameObligation),
				"Obligations differ");
	}

	public static void assertAssociatedAdvice(AssociatedAdviceType a, AssociatedAdviceType b)
	{
		if(a == null && b == null){
			return;
		}
		if(a == null){
			a = new AssociatedAdviceType();
		}
		if(b == null){
			b = new AssociatedAdviceType();
		}
		List<AdviceType> aa = a.getAdvice();
		List<AdviceType> ab = b.getAdvice();
		assertTrue(sameMultiset(aa, ab, Xacml30TestUtility::sameAdvice),
				"Associated advice differs");
	}

	public static void assertAdvice(AdviceType a, AdviceType b)
	{
		assertEquals(a.getAdviceId(), b.getAdviceId());
		assertTrue(sameMultiset(a.getAttributeAssignment(), b.getAttributeAssignment(),
				Xacml30TestUtility::sameAttributeAssignment), "Advice assignments differ");
	}

	public static void assertObligation(ObligationType a, ObligationType b)
	{
		assertEquals(a.getObligationId(), b.getObligationId());
		assertTrue(sameMultiset(a.getAttributeAssignment(), b.getAttributeAssignment(),
				Xacml30TestUtility::sameAttributeAssignment), "Obligation assignments differ");
	}

	public static void assertAttributeAssignment(AttributeAssignmentType a, AttributeAssignmentType b)
	{
		assertEquals(a.getAttributeId(), b.getAttributeId());
		assertEquals(a.getCategory(), b.getCategory());
		assertEquals(a.getIssuer(), b.getIssuer());
		assertEquals(a.getDataType(), b.getDataType());
		assertEquals(a.getOtherAttributes(), b.getOtherAttributes());
		assertTrue(sameValueContent(a.getDataType(), a.getContent(), b.getContent()), "Assignment values differ");
	}

	private static boolean sameAdvice(AdviceType a, AdviceType b)
	{
		return Objects.equals(a.getAdviceId(), b.getAdviceId())
				&& sameMultiset(a.getAttributeAssignment(), b.getAttributeAssignment(),
						Xacml30TestUtility::sameAttributeAssignment);
	}

	private static boolean sameObligation(ObligationType a, ObligationType b)
	{
		return Objects.equals(a.getObligationId(), b.getObligationId())
				&& sameMultiset(a.getAttributeAssignment(), b.getAttributeAssignment(),
						Xacml30TestUtility::sameAttributeAssignment);
	}

	private static boolean sameAttributeAssignment(AttributeAssignmentType a, AttributeAssignmentType b)
	{
		return Objects.equals(a.getAttributeId(), b.getAttributeId())
				&& Objects.equals(a.getCategory(), b.getCategory())
				&& Objects.equals(a.getIssuer(), b.getIssuer())
				&& Objects.equals(a.getDataType(), b.getDataType())
				&& Objects.equals(a.getOtherAttributes(), b.getOtherAttributes())
				&& sameValueContent(a.getDataType(), a.getContent(), b.getContent());
	}

	static <T> boolean sameMultiset(List<T> a, List<T> b, BiPredicate<T, T> equivalent)
	{
		if (a.size() != b.size()) {
			return false;
		}
		boolean[] matched = new boolean[b.size()];
		for (T expected : a) {
			boolean found = false;
			for (int i = 0; i < b.size(); i++) {
				if (!matched[i] && equivalent.test(expected, b.get(i))) {
					matched[i] = true;
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static ResponseType getResponse(String resourcePath) throws Exception
	{
		InputStream in = null;
		try {
			in = getClasspathResource(resourcePath).get();
			assertNotNull(in);
			return ((JAXBElement<ResponseType>) context.createUnmarshaller().unmarshal(in)).getValue();
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	public static RequestContext getRequest(String resourcePath) throws Exception {
		InputStream in = null;
		try {
			in = getClasspathResource(resourcePath).get();
			return requestUnmarshaller.unmarshal(in);
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	public static Supplier<InputStream> getClasspathResource(final String resourcePath) {
		return new Supplier<InputStream>() {
			@Override
			public InputStream get() {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				return cl.getResourceAsStream(resourcePath);
			}
		};
	}
}
