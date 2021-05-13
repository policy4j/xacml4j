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

import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.context.StatusType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.oasis.xacml.v20.jaxb.policy.ObligationsType;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.marshal.jaxb.JAXBContextUtil;
import org.xacml4j.v30.marshal.jaxb.Xacml20RequestContextUnmarshaller;


public class Xacml20TestUtility
{
	private static final JAXBContext context = JAXBContextUtil.getInstance();
	private static final Xacml20RequestContextUnmarshaller requestUnmarshaller = new Xacml20RequestContextUnmarshaller();

	/** Private constructor for utility class */
	private Xacml20TestUtility() {}

	public static void assertResponse(ResponseType a, ResponseType b)
	{
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
		assertEquals(a.getStatusCode().getValue(), b.getStatusCode().getValue());
	}

	public static void assertObligations(ObligationsType a, ObligationsType b)
	{
		if(a == null &&
				b == null){
			return;
		}
		List<ObligationType> oa = a.getObligation();
		List<ObligationType> ob = a.getObligation();
		assertEquals(oa.size(), ob.size());
		Map<String , ObligationType> aMap = toObligationMap(oa);
		Map<String , ObligationType> bMap = toObligationMap(ob);

		assertTrue(aMap.keySet().containsAll(bMap.keySet()));
		assertTrue(bMap.keySet().containsAll(aMap.keySet()));

		for(Map.Entry<String, ObligationType> entry : aMap.entrySet()){
			ObligationType obligationA = entry.getValue();
			ObligationType obligationB = bMap.get(entry.getKey());
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
		for (Map.Entry<String, AttributeAssignmentType> entry : aMap.entrySet()) {
			AttributeAssignmentType attrA = entry.getValue();
			AttributeAssignmentType attrB = bMap.get(entry.getKey());
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
		InputStream in = getClasspathResource(resourcePath);
		assertNotNull(in);
		return ((JAXBElement<ResponseType>)context.createUnmarshaller().unmarshal(in)).getValue();
	}

	public static RequestContext getRequest(String resourcePath) throws Exception {
		return requestUnmarshaller.unmarshal(getClasspathResource(resourcePath));
	}

	public static InputStream getClasspathResource(String resourcePath) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return cl.getResourceAsStream(resourcePath);
	}

}
