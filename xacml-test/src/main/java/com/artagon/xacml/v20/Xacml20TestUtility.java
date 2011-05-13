package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.oasis.xacml.v20.jaxb.context.ObjectFactory;
import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.context.StatusType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.oasis.xacml.v20.jaxb.policy.ObligationsType;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;

public class Xacml20TestUtility 
{
	private static JAXBContext context;
	private static Xacml20RequestContextUnmarshaller requestUnmarshaller;
	static{
		try{
			context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
			requestUnmarshaller = new Xacml20RequestContextUnmarshaller(context);
		}catch(JAXBException e){
			
		}
	}
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
