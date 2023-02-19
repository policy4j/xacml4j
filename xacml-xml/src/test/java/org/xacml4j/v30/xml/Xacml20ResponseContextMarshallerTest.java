package org.xacml4j.v30.xml;

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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.oasis.xacml.v20.jaxb.context.ResultType;
import org.oasis.xacml.v20.jaxb.policy.AttributeAssignmentType;
import org.oasis.xacml.v20.jaxb.policy.ObligationType;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.request.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshal.Marshaller;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Supplier;
import com.google.common.io.Closeables;


public class Xacml20ResponseContextMarshallerTest {

	private static Marshaller<ResponseContext> responseMarshaller;
	private static Unmarshaller<RequestContext> requestUnmarshaller;
	private static Unmarshaller<CompositeDecisionRule> policyUnmarshaller;
	private static PolicyRepository repository;

	private PolicyDecisionPointBuilder pdpBuilder;

	@BeforeClass
	public static void init_static() throws Exception
	{
		repository = new InMemoryPolicyRepository(
				"testRepositoryId",
				FunctionProvider.builder()
				.withDefaultFunctions()
				.build(),
				DecisionCombiningAlgorithmProvider.builder()
				                                          .withDefaultAlgorithms()
				                                          .build());
		responseMarshaller = new Xacml20ResponseContextMarshaller();
		requestUnmarshaller = new Xacml20RequestContextUnmarshaller();
		policyUnmarshaller = new Xacml30PolicyUnmarshaller();
	}

	@Before
	public void init(){
		this.pdpBuilder = PolicyDecisionPointBuilder.builder("testPdp")
		.pip(PolicyInformationPointBuilder.builder("testPip")
				.defaultResolvers()
				.build())
		.policyRepository(repository);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMarshaller() throws Exception
	{
		RequestContext request = getRequest("MarshallerTestRequest.xml");
		final PolicyDecisionPoint pdp = pdpBuilder
				.rootPolicy(
						repository.newImportTool(policyUnmarshaller)
						          .importPolicy(getPolicy()))
				.build();
		ResponseContext response = pdp.decide(request);
		ResponseType actual = ((JAXBElement<ResponseType>)responseMarshaller.marshal(response)).getValue();
		assertEquals(actual.getResult().size(), 1);
		List<ResultType> actualResults = actual.getResult();
		for(int i = 0; i < actualResults.size(); i++){
			assertEquals(actualResults.get(i).getStatus().getStatusCode().getValue(), "urn:oasis:names:tc:xacml:1.0:status:ok");
			
			List<ObligationType> actualObligations = actualResults.get(i).getObligations().getObligation();

			assertEquals(actualObligations.size(), 1);
			
			Map<String, ObligationType> aMap = new LinkedHashMap<String, ObligationType>();
			for(ObligationType o : actualObligations){
				aMap.put(o.getObligationId(), o);
			}

			for(String obligationId : aMap.keySet()){
				ObligationType obligationA = aMap.get(obligationId);
				List<AttributeAssignmentType> aAttr = obligationA.getAttributeAssignment();
				assertEquals(aAttr.size(), 18);
			}
		}
	}

	private static Supplier<InputStream> getPolicy() {
		String path = "MarshallerTestPolicy.xml";
		return getClasspathResource(path);
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

	public static RequestContext getRequest(String resourcePath) throws Exception {
		InputStream in = null;
		try {
			in = getClasspathResource(resourcePath).get();
			return requestUnmarshaller.unmarshal(in);
		} finally {
			Closeables.closeQuietly(in);
		}
	}
	
}
