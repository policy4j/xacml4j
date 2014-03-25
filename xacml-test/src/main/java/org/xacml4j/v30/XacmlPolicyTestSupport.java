package org.xacml4j.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.marshal.jaxb.Xacml20RequestContextUnmarshaller;
import org.xacml4j.v30.marshal.jaxb.Xacml20ResponseContextUnmarshaller;
import org.xacml4j.v30.marshal.jaxb.Xacml30RequestContextUnmarshaller;
import org.xacml4j.v30.marshal.jaxb.Xacml30ResponseContextUnmarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPointBuilder;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Preconditions;

public class XacmlPolicyTestSupport {
	protected final Logger log = LoggerFactory
			.getLogger(XacmlPolicyTestSupport.class);

	private Xacml30RequestContextUnmarshaller requestUnmarshaller;
	private Xacml30ResponseContextUnmarshaller responseUnmarshaller;
	private Xacml20ResponseContextUnmarshaller xacml20ResponseUnmarshaller;
	private Xacml20RequestContextUnmarshaller xacml20RequestUnmarshaller;
	
	@Before
	public void setup() throws Exception {
		this.requestUnmarshaller = new Xacml30RequestContextUnmarshaller();
		this.responseUnmarshaller = new Xacml30ResponseContextUnmarshaller();
		this.xacml20ResponseUnmarshaller = new Xacml20ResponseContextUnmarshaller();
		this.xacml20RequestUnmarshaller = new Xacml20RequestContextUnmarshaller();

	}

	protected XacmlTestPdpBuilder builder(String rootPolicyId, String rootPolicyVersion)
	{
		XacmlTestPdpBuilder pdpBuilder = new XacmlTestPdpBuilder("testPDP", "testPIP", "testRepositoryId");
		pdpBuilder.withRootPolicy(rootPolicyId, rootPolicyVersion);
		pdpBuilder.withDefaultResolvers();
		pdpBuilder.withDefaultFunctions();
		pdpBuilder.withDefaultDecisionAlgorithms();
		return pdpBuilder;
	}

	protected void verifyXacml30Response(PolicyDecisionPoint pdp,
			String xacmlRequestResource,
			String expectedXacmlResponseResource) throws Exception {
		RequestContext req = getXacml30Request(xacmlRequestResource);
		ResponseContext expectedResponse = getXacml30Response(expectedXacmlResponseResource);
		ResponseContext resp = pdp.decide(req);
		log.debug("Expected xacml 30 respone=\"{}\"", expectedResponse);
		log.debug("Received xacml 30 respone=\"{}\"", resp);
		assertResponse(expectedResponse, resp);
	}

	protected void verifyXacml20Response(
			PolicyDecisionPoint pdp,
			String xacmlRequestResource,
			String expectedXacmlResponseResource) throws Exception {
		RequestContext req = getXacml20Request(xacmlRequestResource);
		assertNotNull(xacmlRequestResource, req);
		ResponseContext expectedResponse = getXacml20Response(expectedXacmlResponseResource);
		assertNotNull(expectedXacmlResponseResource, expectedResponse);
		ResponseContext resp = pdp.decide(req);
		if(log.isDebugEnabled()){
			log.debug(resp.toString());
		}
		assertResponse(expectedResponse, resp);
	}

	protected InputStream getResource(String resourcePath, ClassLoader cl) {
		log.debug("Loading resource \"{}\"", resourcePath);
		InputStream in = cl.getResourceAsStream(resourcePath);
		if(in  == null){
			throw new IllegalArgumentException(String.format(
					"Failed to load resource from path=\"%s\"", resourcePath));
		}
		return in;
	}

	protected InputStream getResource(String resourcePath) {
		return getResource(resourcePath, Thread.currentThread()
				.getContextClassLoader());
	}

	protected InputStream getPolicy(String path){
		if(log.isDebugEnabled()){
			log.debug("Loading policy from path=\"{}\"", path);
		}
		return getResource(path);
	}

	protected ResponseContext getXacml30Response(String resourcePath) throws Exception {
		return responseUnmarshaller.unmarshal(getResource(resourcePath));
	}

	protected ResponseContext getXacml20Response(String resourcePath) throws Exception {
		return xacml20ResponseUnmarshaller.unmarshal(getResource(resourcePath));
	}

	protected RequestContext getXacml20Request(String path) throws Exception {
		return xacml20RequestUnmarshaller.unmarshal(getResource(path));
	}

	protected RequestContext getXacml30Request(String path) throws Exception {
		return requestUnmarshaller.unmarshal(getResource(path));
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
			this.rootPolicyVersion = version;
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
			functionProviderBuilder.defaultFunctions();
			return this;
		}

		public XacmlTestPdpBuilder withFunctionProvider(Object provider){
			functionProviderBuilder.fromInstance(provider);
			return this;
		}

		public XacmlTestPdpBuilder withFunctionProvider(Class<?> clazz){
			functionProviderBuilder.fromClass(clazz);
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

		public XacmlTestPdpBuilder withPolicies(InputStream ... policies){
			for(InputStream s : policies){
				this.policies.add(s);
			}
			return this;
		}

		public XacmlTestPdpBuilder withPolicyFromClasspath(String path ){
			InputStream in = getPolicy(path);
			this.policies.add(in);
			return this;
		}

		public XacmlTestPdpBuilder withResolver(Object resolver){
			pipBuilder.withResolver(resolver);
			return this;
		}

		public XacmlTestPdpBuilder withResolver(String policyId, Object resolver){
			pipBuilder.withResolver(resolver);
			return this;
		}

		public PolicyDecisionPoint build() throws Exception
		{
			PolicyRepository repository = new InMemoryPolicyRepository(
					repositoryId,
					functionProviderBuilder.build(),
					decisionAlgoProviderBuilder.create());
			for(InputStream in : policies){
				repository.importPolicy(in);
			}
			return PolicyDecisionPointBuilder
					.builder(pdpId)
					.pip(pipBuilder.build())
					.policyRepository(repository)
					.defaultRequestHandlers()
					.rootPolicy(repository.get(rootPolicyId, Version.parse(rootPolicyVersion)))
					.build();
		}
	}
}
