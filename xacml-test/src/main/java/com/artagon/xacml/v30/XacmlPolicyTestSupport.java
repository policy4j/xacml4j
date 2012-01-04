package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.marshall.jaxb.Xacml30RequestContextUnmarshaller;
import com.artagon.xacml.v30.marshall.jaxb.Xacml30ResponseContextUnmarshaller;
import com.artagon.xacml.v30.pdp.Advice;
import com.artagon.xacml.v30.pdp.Attribute;
import com.artagon.xacml.v30.pdp.AttributeExp;
import com.artagon.xacml.v30.pdp.AttributeExpType;
import com.artagon.xacml.v30.pdp.Attributes;
import com.artagon.xacml.v30.pdp.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v30.pdp.Obligation;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointBuilder;
import com.artagon.xacml.v30.pdp.RequestContext;
import com.artagon.xacml.v30.pdp.ResponseContext;
import com.artagon.xacml.v30.pdp.Result;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import com.artagon.xacml.v30.spi.function.FunctionProviderBuilder;
import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPointBuilder;
import com.artagon.xacml.v30.spi.repository.InMemoryPolicyRepository;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.google.common.base.Preconditions;

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
			String rootPolicyId, String rootPolicyVersion) throws Exception {
		return buildPDP(getDefaultTestPolicies(), attributeResolvers, null, null, rootPolicyId, rootPolicyVersion);
	}

	protected PolicyDecisionPoint buildPDP(String rootPolicyId, String rootPolicyVersion) throws Exception {
		return buildPDP(getDefaultTestPolicies(), null, null, null, rootPolicyId, rootPolicyVersion);
	}
	
	
	protected XacmlTestPdpBuilder pdpBuilder(String rootPolicyId, String rootPolicyVersion)
	{
		XacmlTestPdpBuilder pdpBuilder = new XacmlTestPdpBuilder("testPDP", "testPIP", "testRepositoryId");
		pdpBuilder.withRootPolicy(rootPolicyId, rootPolicyVersion);
		pdpBuilder.withDefaultResolvers();
		pdpBuilder.withDefaultFunctions();
		pdpBuilder.withDefaultDecisionAlgorithms();
		return pdpBuilder;
	}
	
	protected PolicyDecisionPoint buildPDP(
			InputStream[] policyResources,
			Object [] attributeResolvers,
			Object[] functionProviders,
			Object[] decisionAlgoProviders,
			String rootPolicyId, String rootPolicyVersion) throws Exception 
	{
		XacmlTestPdpBuilder pdpBuilder = pdpBuilder(rootPolicyId, rootPolicyVersion);
		if(attributeResolvers != null) {
			for(Object r: attributeResolvers) {
				pdpBuilder.withResolver(r);
			}
		 }
		if(functionProviders != null){
			for(Object f : functionProviders){
				pdpBuilder.withFunctionProvider(f);
			}
		}
		if(decisionAlgoProviders != null){
			for(Object decisionAlgoProvider : decisionAlgoProviders){
				pdpBuilder.withDecisionAlgorithmProvider(decisionAlgoProvider);
			}
		}
		for (InputStream path : policyResources) {
			pdpBuilder.withPolicy(path);
		}
		return pdpBuilder.build();
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
	
	
	public class XacmlTestPdpBuilder
	{
		private String rootPolicyId;
		private String rootPolicyVersion;
		private String pdpId;
		private String repositoryId;
		private FunctionProviderBuilder functionProviderBuilder;
		private PolicyInformationPointBuilder pipBuilder;
		private DecisionCombiningAlgorithmProviderBuilder decisionAlgoProviderBuilder;
		private Collection<InputStream> policies;
		
		public XacmlTestPdpBuilder(String pdpId, String pipId, String repositoryId){
			Preconditions.checkNotNull(pdpId);
			Preconditions.checkNotNull(pipId);
			Preconditions.checkNotNull(repositoryId);
			this.functionProviderBuilder = FunctionProviderBuilder.builder();
			this.decisionAlgoProviderBuilder = DecisionCombiningAlgorithmProviderBuilder.builder();
			this.pipBuilder = PolicyInformationPointBuilder.builder(pipId);
			this.policies = new LinkedList<InputStream>();
			this.repositoryId = repositoryId;
			this.pdpId = pdpId;
		}
		
		public XacmlTestPdpBuilder withRootPolicy(String policyId, String version){
			this.rootPolicyId = policyId;
			this.rootPolicyVersion = rootPolicyVersion;
			return this;
		}
		
		public XacmlTestPdpBuilder withDefaultDecisionAlgorithms(){
			decisionAlgoProviderBuilder.withDefaultAlgorithms();
			return this;
		}
		
		public XacmlTestPdpBuilder withDefaultResolvers(){
			pipBuilder.withDefaultResolvers();
			return this;
		}
		
		public XacmlTestPdpBuilder withDefaultFunctions(){
			functionProviderBuilder.withDefaultFunctions();
			return this;
		}
		
		public XacmlTestPdpBuilder withFunctionProvider(Object provider){
			functionProviderBuilder.withFunctionsFromInstance(provider);
			return this;
		}
		
		public XacmlTestPdpBuilder withFunctionProvider(Class<?> clazz){
			functionProviderBuilder.withFunctionsFromClass(clazz);
			return this;
		}
		
		public XacmlTestPdpBuilder withDecisionAlgorithmProvider(Object provider){
			decisionAlgoProviderBuilder.withAlgorithmProvider(provider);
			return this;
		}
		
		public XacmlTestPdpBuilder withPolicy(InputStream stream){
			this.policies.add(stream);
			return this;
		}
		
		public XacmlTestPdpBuilder withResolver(Object resolver){
			pipBuilder.withResolver(resolver);
			return this;
		}
		
		public PolicyDecisionPoint build() throws Exception
		{
			PolicyRepository repository = new InMemoryPolicyRepository(
					repositoryId, 
					functionProviderBuilder.build(), 
					decisionAlgoProviderBuilder.build());
			for(InputStream in : policies){
				repository.importPolicy(in);
			}
			return PolicyDecisionPointBuilder
					.builder(pdpId)
					.withPolicyInformationPoint(pipBuilder.build())
					.withPolicyRepository(repository)
					.withDefaultRequestHandlers()
					.withRootPolicy(repository.get(rootPolicyId, Version.parse(rootPolicyVersion)))
					.build();
		}
	}
}
