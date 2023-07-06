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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v30.jaxb.AttributeType;
import org.oasis.xacml.v30.jaxb.AttributeValueType;
import org.oasis.xacml.v30.jaxb.AttributesType;
import org.oasis.xacml.v30.jaxb.IdReferenceType;
import org.oasis.xacml.v30.jaxb.PolicyIdentifierListType;
import org.oasis.xacml.v30.jaxb.ResponseType;
import org.oasis.xacml.v30.jaxb.ResultType;
import org.oasis.xacml.v30.jaxb.StatusType;
import org.oasis.xacml.v30.jaxb.AttributeAssignmentType;
import org.oasis.xacml.v30.jaxb.ObligationType;
import org.oasis.xacml.v30.jaxb.ObligationsType;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.xml.JAXBUtils;
import org.xacml4j.v30.xml.Xacml30RequestContextUnmarshaller;

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
	}

	public static void assertAttributesType(List<AttributesType> a, List<AttributesType> b){
		assertEquals(a.size(), b.size());
		for(int i = 0; i < a.size(); i++){
			AttributesType a1 = a.get(i);
			AttributesType b1 = b.get(i);
			assertEquals(a1.getCategory(), b1.getCategory());
			assertEquals(a1.getId(), b1.getId());
			assertAttributeType(a1.getAttribute(), b1.getAttribute());
			assertEquals(a1.getContent().getContent(), b1.getContent().getContent());
		}
	}

	public static void assertAttributeType(List<AttributeType> a, List<AttributeType> b){
		assertEquals(a.size(), b.size());
		for(int i = 0; i < a.size(); i++){
			AttributeType a1 = a.get(i);
			AttributeType b1 = b.get(i);
			assertEquals(a1.getAttributeId(), b1.getAttributeId());
			assertEquals(a1.getIssuer(), b1.getIssuer());
			assertEquals(a1.isIncludeInResult(), b1.isIncludeInResult());
			assertAttributeValues(a1.getAttributeValue(), b1.getAttributeValue());
		}
	}

	public static void assertAttributeValues(List<AttributeValueType> a, List<AttributeValueType> b){
		assertEquals(a.size(), b.size());
		for(int i = 0; i < a.size(); i++){
			AttributeValueType a1 = a.get(i);
			AttributeValueType b1 = b.get(i);
			assertEquals(a1.getDataType(), b1.getDataType());
			assertEquals(a1.getContent(), b1.getContent());
			assertEquals(a1.getOtherAttributes(), b1.getOtherAttributes());
		}
	}

	public static void assertPolicyIdentifierList(PolicyIdentifierListType a, PolicyIdentifierListType b){
		if(a == null && b == null){
			return;
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
		List<ObligationType> oa = a.getObligation();
		List<ObligationType> ob = a.getObligation();
		assertEquals(oa.size(), ob.size());
		Map<String , ObligationType> aMap = toObligationMap(oa);
		Map<String , ObligationType> bMap = toObligationMap(ob);

		assertTrue(aMap.keySet().containsAll(bMap.keySet()));
		assertTrue(bMap.keySet().containsAll(aMap.keySet()));

		for(String obligationId : aMap.keySet()){
			ObligationType obligationA = aMap.get(obligationId);
			ObligationType obligationB = bMap.get(obligationId);
			assertObligation(obligationA, obligationB);
		}
	}

	public static void assertObligation(ObligationType a, ObligationType b)
	{
		List<AttributeAssignmentType> aAttr = a.getAttributeAssignment();
		List<AttributeAssignmentType> bAttr = b.getAttributeAssignment();
		assertEquals(aAttr.size(), bAttr.size());
		Map<String, AttributeAssignmentType> aMap = toAttributeAssignmentMap(aAttr);
		Map<String, AttributeAssignmentType> bMap = toAttributeAssignmentMap(bAttr);
		assertTrue(aMap.keySet().containsAll(bMap.keySet()));
		assertTrue(bMap.keySet().containsAll(aMap.keySet()));
		for(String attributeId : aMap.keySet()){
			AttributeAssignmentType attrA = aMap.get(attributeId);
			AttributeAssignmentType attrB = bMap.get(attributeId);
			assertAttributeAssignment(attrA, attrB);
		}
	}

	public static void assertAttributeAssignment(AttributeAssignmentType a, AttributeAssignmentType b)
	{
		assertEquals(a.getAttributeId(), b.getAttributeId());
		assertEquals(a.getDataType(), b.getDataType());
		assertEquals(a.getContent(), b.getContent());
	}

	private static Map<String, ObligationType> toObligationMap(
			List<ObligationType> obligations)
	{
		Map<String, ObligationType> map = new LinkedHashMap<String, ObligationType>();
		for(ObligationType o : obligations){
			map.put(o.getObligationId(), o);
		}
		return map;
	}

	private static Map<String, AttributeAssignmentType> toAttributeAssignmentMap(
			List<AttributeAssignmentType> attributes)
	{
		Map<String, AttributeAssignmentType> map = new LinkedHashMap<String, AttributeAssignmentType>();
		for(AttributeAssignmentType a : attributes){
			map.put(a.getAttributeId(), a);
		}
		return map;
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
