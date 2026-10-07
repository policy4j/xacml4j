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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.context.StatusCodeType;
import org.oasis.xacml.v20.jaxb.context.StatusType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.oasis.xacml.v20.jaxb.policy.ObligationsType;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.xml.JAXBUtils;
import org.xacml4j.v30.xml.Xacml20RequestContextUnmarshaller;

import com.google.common.base.Supplier;
import com.google.common.io.Closeables;


public class Xacml20TestUtility
{
	private static final JAXBContext context = JAXBUtils.getInstance();
	private static final Xacml20RequestContextUnmarshaller requestUnmarshaller = new Xacml20RequestContextUnmarshaller();

	/** Private constructor for utility class */
	private Xacml20TestUtility() {}

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
		assertEquals(a.getResourceId(), b.getResourceId());
		assertStatus(a.getStatus(), b.getStatus());
		assertObligations(a.getObligations(), b.getObligations());
	}

	public static void assertStatus(StatusType a, StatusType b)
	{
		if (a == null || b == null) {
			assertEquals(a, b, "Status presence differs");
			return;
		}
		assertStatusCode(a.getStatusCode(), b.getStatusCode());
		assertTrue(Xacml30TestUtility.sameContent(
				a.getStatusDetail() == null ? List.of() : a.getStatusDetail().getAny(),
				b.getStatusDetail() == null ? List.of() : b.getStatusDetail().getAny()), "Status details differ");
	}

	private static void assertStatusCode(StatusCodeType a, StatusCodeType b)
	{
		if (a == null || b == null) {
			assertEquals(a, b, "Status code presence differs");
			return;
		}
		assertEquals(a.getValue(), b.getValue());
		assertStatusCode(a.getStatusCode(), b.getStatusCode());
	}

	public static void assertObligations(ObligationsType a, ObligationsType b)
	{
		List<ObligationType> oa = a == null ? List.of() : a.getObligation();
		List<ObligationType> ob = b == null ? List.of() : b.getObligation();
		assertTrue(Xacml30TestUtility.sameMultiset(oa, ob, Xacml20TestUtility::sameObligation), "Obligations differ");
	}

	public static void assertObligation(ObligationType a, ObligationType b)
	{
		assertEquals(a.getObligationId(), b.getObligationId());
		assertEquals(a.getFulfillOn(), b.getFulfillOn());
		assertTrue(Xacml30TestUtility.sameMultiset(a.getAttributeAssignment(), b.getAttributeAssignment(),
				Xacml20TestUtility::sameAttributeAssignment), "Obligation assignments differ");
	}

	public static void assertAttributeAssignment(AttributeAssignmentType a, AttributeAssignmentType b)
	{
		assertEquals(a.getAttributeId(), b.getAttributeId());
		assertEquals(a.getDataType(), b.getDataType());
		assertEquals(a.getOtherAttributes(), b.getOtherAttributes());
		assertTrue(Xacml30TestUtility.sameValueContent(a.getDataType(), a.getContent(), b.getContent()),
				"Assignment values differ");
	}

	private static boolean sameObligation(ObligationType a, ObligationType b)
	{
		return Objects.equals(a.getObligationId(), b.getObligationId())
				&& Objects.equals(a.getFulfillOn(), b.getFulfillOn())
				&& Xacml30TestUtility.sameMultiset(a.getAttributeAssignment(), b.getAttributeAssignment(),
						Xacml20TestUtility::sameAttributeAssignment);
	}

	private static boolean sameAttributeAssignment(AttributeAssignmentType a, AttributeAssignmentType b)
	{
		return Objects.equals(a.getAttributeId(), b.getAttributeId())
				&& Objects.equals(a.getDataType(), b.getDataType())
				&& Objects.equals(a.getOtherAttributes(), b.getOtherAttributes())
				&& Xacml30TestUtility.sameValueContent(a.getDataType(), a.getContent(), b.getContent());
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
