package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.core.AttributeCategory;
import com.artagon.xacml.v30.core.Version;
import com.artagon.xacml.v30.marshall.jaxb.Xacml30RequestContextUnmarshaller;
import com.artagon.xacml.v30.marshall.jaxb.Xacml30ResponseContextUnmarshaller;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointBuilder;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import com.artagon.xacml.v30.spi.function.FunctionProvider;
import com.artagon.xacml.v30.spi.function.FunctionProviderBuilder;
import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPointBuilder;
import com.artagon.xacml.v30.spi.repository.InMemoryPolicyRepository;

public class XacmlPolicyTestSupport {
	protected final Logger log = LoggerFactory
			.getLogger(XacmlPolicyTestSupport.class);
	private Xacml30RequestContextUnmarshaller requestUnmarshaller;
	private Xacml30ResponseContextUnmarshaller responseUnmarshaller;

	@Before
	public void setup() throws Exception {
		requestUnmarshaller = new Xacml30RequestContextUnmarshaller();
		responseUnmarshaller = new Xacml30ResponseContextUnmarshaller();
	}

	protected InputStream[] getDefaultTestPolicies() throws Exception {
		return new InputStream[0];
	}

	protected PolicyDecisionPoint buildPDP(
			AttributeResolver [] attributeResolvers,
			String rootPolicyId, Version rootPolicyVersion) throws Exception {
		return buildPDP(getDefaultTestPolicies(), attributeResolvers, null, null, rootPolicyId, rootPolicyVersion);
	}

	protected PolicyDecisionPoint buildPDP(String rootPolicyId, Version rootPolicyVersion) throws Exception {
		return buildPDP(getDefaultTestPolicies(), null, null, null, rootPolicyId, rootPolicyVersion);
	}
	
	protected PolicyDecisionPoint buildPDP(
			InputStream[] policyResources,
			AttributeResolver [] attributeResolvers,
			FunctionProvider[] functionProviders,
			DecisionCombiningAlgorithmProvider[] decisionAlgo,
			String rootPolicyId, Version rootPolicyVersion) throws Exception {

		 PolicyInformationPointBuilder pipBuilder = PolicyInformationPointBuilder
				.builder("testPIP")
				.withDefaultResolvers();
		 if(attributeResolvers != null) {
			for(AttributeResolver r: attributeResolvers) {
				pipBuilder.withResolver(r);
			}
		 }
		
		FunctionProviderBuilder functions = FunctionProviderBuilder
				.builder()
				.withDefaultFunctions();
		if(functionProviders != null){
			for(FunctionProvider f : functionProviders){
				functions.withFunctions(f);
			}
		}
		DecisionCombiningAlgorithmProviderBuilder algorithms = DecisionCombiningAlgorithmProviderBuilder
				.builder()
				.withDefaultAlgorithms();
		if(decisionAlgo != null){
			for(DecisionCombiningAlgorithmProvider algo : decisionAlgo){
				algorithms.withAlgorithmProvider(algo);
			}
		}
		PolicyInformationPoint pip = pipBuilder.build();
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository(
				"testRepositoryId", functions.build(), 
				algorithms.build());
		for (InputStream path : policyResources) {
			repository.importPolicy(path);
		}

		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder
				.builder("testPDP")
				.withPolicyInformationPoint(pip)
				.withPolicyRepository(repository)
				.withDefaultRequestHandlers()
				.withRootPolicy(repository.get(rootPolicyId, rootPolicyVersion))
				.build();

		return pdp;
	}

	protected void verifyResponse(PolicyDecisionPoint pdp, String xacmlRequestResource, String expectedXacmlResponseResource) throws Exception {
		RequestContext req = getRequest(xacmlRequestResource);
		ResponseContext expectedResponse = getResponse(expectedXacmlResponseResource);
		ResponseContext resp = pdp.decide(req);

		assertResponse(expectedResponse, resp);
	}

	protected InputStream getResource(String resourcePath, ClassLoader cl) {
		log.debug("Loading resource \"{}\"", resourcePath);
		return cl.getResourceAsStream(resourcePath);
	}

	protected InputStream getResource(String resourcePath) {
		return getResource(resourcePath, Thread.currentThread()
				.getContextClassLoader());
	}

	protected InputStream getPolicy(String path) throws Exception {
		log.debug("Loading policy: {}", path);
		return getResource(path);
	}

	protected ResponseContext getResponse(String resourcePath) throws Exception {
		return responseUnmarshaller.unmarshal(getResource(resourcePath));
	}

	protected RequestContext getRequest(String path) throws Exception {
		return requestUnmarshaller.unmarshal(getResource(path));
	}

	protected AttributeResolver createAttributeResolverFor(AttributeCategory attrCategory, Attribute ...attributes) {
		return new ExpectedAttributeResolver(attrCategory, attributes);
	}

	protected <T extends AttributeExpType> Attribute createAttribute(
			String attributeId, T type, Object... values) {
		Collection<AttributeExp> attrValues = new LinkedList<AttributeExp>();
		for(Object value : values) {
			attrValues.add(type.create(value));
		}
		return new Attribute(attributeId, attrValues);
	}

	public static void assertResponse(ResponseContext resp1,
			ResponseContext resp2) {
		assertEquals(resp1.getResults().size(), resp2.getResults().size());
		Iterator<Result> r1It = resp1.getResults().iterator();
		Iterator<Result> r2It = resp2.getResults().iterator();
		while (r1It.hasNext()) {
			Result r1 = r1It.next();
			Result r2 = r2It.next();
			assertResult(r1, r2);
		}

	}

	private static void assertResult(Result r1, Result r2) {
		assertEquals(r1.getDecision(), r2.getDecision());
		assertEquals(r1.getStatus(), r2.getStatus());
		assertObligations(r1.getObligations(), r2.getObligations());
		assertAssociatedAdvices(r1.getAssociatedAdvice(),
				r2.getAssociatedAdvice());
		assertAttributes(r1.getIncludeInResultAttributes(), r2.getIncludeInResultAttributes());
		assertPolicyIdentifiers(r1.getPolicyIdentifiers(),
				r2.getPolicyIdentifiers());
	}

	private static interface Matcher<T> {
		boolean matches(T o1, T o2);
	}

	private static void assertObligations(Collection<Obligation> o1,
			Collection<Obligation> o2) {
		assertArrayEquals(o1, o2, new Matcher<Obligation>() {
			@Override
			public boolean matches(Obligation o1, Obligation o2) {
				return o1.getId().equals(o2.getId());
			}
		});
	}

	private static void assertAssociatedAdvices(Collection<Advice> a1,
			Collection<Advice> a2) {
		assertArrayEquals(a1, a2, new Matcher<Advice>() {
			@Override
			public boolean matches(Advice o1, Advice o2) {
				return o1.getId().equals(o2.getId());
			}
		});
	}

	private static void assertAttributes(Collection<Attributes> a1,
			Collection<Attributes> a2) {
		assertArrayEquals(a1, a2, new Matcher<Attributes>() {
			@Override
			public boolean matches(Attributes o1, Attributes o2) {
				return o1.getId().equals(o2.getId()) && o1.getCategory().equals(o2.getCategory());
			}
		});
	}

	private static void assertPolicyIdentifiers(
			Collection<CompositeDecisionRuleIDReference> p1,
			Collection<CompositeDecisionRuleIDReference> p2) {
		assertArrayEquals(p1, p2, new Matcher<CompositeDecisionRuleIDReference>() {
			@Override
			public boolean matches(CompositeDecisionRuleIDReference o1, CompositeDecisionRuleIDReference o2) {
				return o1.getId().equals(o2.getId());
			}
		});
	}

	protected static <T> void assertArrayEquals(Collection<T> a1,
			Collection<T> a2, Matcher<T> matcher) {
		assertEquals(a1.size(), a2.size());
		for(T o: a1) {
			assertEquals(o, findMatchingObject(a2, o, matcher));
		}
	}
	
	private static <T> T findMatchingObject(Collection<T> collection, T object, Matcher<T> matcher) {
		for(T o: collection) {
			if (matcher.matches(o, object)) {
				return o;
			}
		}
		return null;
	}
}
