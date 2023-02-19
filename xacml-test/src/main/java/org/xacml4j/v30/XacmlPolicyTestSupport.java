package org.xacml4j.v30;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.request.RequestContext;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.xml.Xacml20RequestContextUnmarshaller;
import org.xacml4j.v30.xml.Xacml20ResponseContextUnmarshaller;
import org.xacml4j.v30.xml.Xacml30RequestContextUnmarshaller;
import org.xacml4j.v30.xml.Xacml30ResponseContextUnmarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.io.Closeables;
import com.google.common.net.MediaType;

public class XacmlPolicyTestSupport {
	protected final Logger log = LoggerFactory.getLogger(XacmlPolicyTestSupport.class);

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

	protected Builder builder(String rootPolicyId, String rootPolicyVersion)
	{
		Builder pdpBuilder = new Builder("testPDP", "testPIP", "testRepositoryId");
		pdpBuilder.rootPolicy(rootPolicyId, rootPolicyVersion);
		pdpBuilder.standardResolvers();
		pdpBuilder.standardFunctions();
		pdpBuilder.defaultDecisionAlgorithms();
		return pdpBuilder;
	}

	protected void verifyXacml30Response(PolicyDecisionPoint pdp,
			String xacmlRequestResource,
			String expectedXacmlResponseResource) throws Exception {
		RequestContext req = getXacml30Request(xacmlRequestResource);
		ResponseContext expectedResponse = getXacml30Response(expectedXacmlResponseResource);
		ResponseContext resp = pdp.decide(req);
		log.debug("Expected XACML 3.0 response=\"{}\"", expectedResponse);
		log.debug("Received XACML 3.0 response=\"{}\"", resp);
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
		log.debug("Expected XACML 2.0 response=\"{}\"", expectedResponse);
		log.debug("Received XACML 2.0 response=\"{}\"", resp);
		assertResponse(expectedResponse, resp);
	}

	protected Supplier<InputStream> getResource(final String resourcePath, final ClassLoader cl) {
		return new Supplier<InputStream>() {
			@Override
			public InputStream get() {
				log.debug("Loading resource \"{}\"", resourcePath);
				InputStream in = cl.getResourceAsStream(resourcePath);
				if(in  == null){
					throw new IllegalArgumentException(String.format(
							"Failed to load resource from path=\"%s\"", resourcePath));
				}
				return in;
			}
		};
	}

	protected Supplier<InputStream> getResource(String resourcePath) {
		return getResource(resourcePath, Thread.currentThread()
				.getContextClassLoader());
	}

	protected Supplier<InputStream> getPolicy(String path){
		if(log.isDebugEnabled()){
			log.debug("Loading policy from path=\"{}\"", path);
		}
		return getResource(path);
	}

	protected ResponseContext getXacml30Response(String resourcePath) throws Exception {
		InputStream in = null;
		try {
			in = getResource(resourcePath).get();
			return responseUnmarshaller.unmarshal(in);
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	protected ResponseContext getXacml20Response(String resourcePath) throws Exception {
		InputStream in = null;
		try {
			in = getResource(resourcePath).get();
			return xacml20ResponseUnmarshaller.unmarshal(in);
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	protected RequestContext getXacml20Request(String path) throws Exception {
		InputStream in = null;
		try {
			in = getResource(path).get();
			return xacml20RequestUnmarshaller.unmarshal(in);
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	protected RequestContext getXacml30Request(String path) throws Exception {
		InputStream in = null;
		try {
			in = getResource(path).get();
			return requestUnmarshaller.unmarshal(in);
		} finally {
			Closeables.closeQuietly(in);
		}
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

	private static void assertAttributes(Collection<Category> a1,
			Collection<Category> a2) {
		assertArrayEquals(a1, a2, new Matcher<Category>() {
			@Override
			public boolean matches(Category o1, Category o2) {
				return o1.getReferenceId().equals(o2.getReferenceId()) && o1.getCategoryId().equals(o2.getCategoryId());
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


	public class Builder
	{
		private String rootPolicyId;
		private String rootPolicyVersion;
		private String pdpId;
		private String repositoryId;
		private FunctionProviderBuilder functionProviderBuilder;
		private PolicyInformationPoint.Builder pipBuilder;
		private ResolverRegistry.Builder registryBuilder;

		private DecisionCombiningAlgorithmProviderBuilder decisionAlgoProviderBuilder;
		private Collection<Supplier<InputStream>> policies;

		public Builder(String pdpId, String pipId, String repositoryId){
			Preconditions.checkNotNull(pdpId);
			Preconditions.checkNotNull(pipId);
			Preconditions.checkNotNull(repositoryId);
			this.functionProviderBuilder = FunctionProviderBuilder.builder();
			this.registryBuilder = ResolverRegistry.builder();
			this.decisionAlgoProviderBuilder = DecisionCombiningAlgorithmProviderBuilder.builder();
			this.pipBuilder = PolicyInformationPoint.builder(pipId);
			this.policies = new ArrayList<>();
			this.repositoryId = repositoryId;
			this.pdpId = pdpId;
		}

		public Builder rootPolicy(String policyId, String version){
			this.rootPolicyId = policyId;
			this.rootPolicyVersion = version;
			return this;
		}

		public Builder defaultDecisionAlgorithms(){
			decisionAlgoProviderBuilder.withDefaultAlgorithms();
			return this;
		}

		public Builder standardResolvers(){
			registryBuilder.withDefaultResolvers();
			return this;
		}

		public Builder standardFunctions() {
			functionProviderBuilder.withDefaultFunctions();
			return this;
		}

		public Builder policy(Supplier<InputStream> stream){
			this.policies.add(stream);
			return this;
		}

		public Builder policies(Supplier<InputStream> ... policies){
			Collections.addAll(this.policies, policies);
			return this;
		}

		public PolicyDecisionPoint build() throws Exception
		{
			PolicyRepository repository = new InMemoryPolicyRepository(
					repositoryId,
					functionProviderBuilder.build(),
					decisionAlgoProviderBuilder.build(), un);
			for (Supplier<InputStream> in : policies) {
				repository.importPolicy(MediaType.XML_UTF_8, in);
			}
			return PolicyDecisionPointBuilder
					.builder(pdpId)
					.pip(pipBuilder.registry(registryBuilder.build())
					               .build())
					.policyRepository(repository)
					.defaultRequestHandlers()
					.rootPolicy(repository.get(rootPolicyId, Version.parse(rootPolicyVersion)))
					.build();
		}
	}
}
