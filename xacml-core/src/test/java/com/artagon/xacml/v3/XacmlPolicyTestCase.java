package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;

import org.junit.Before;

import com.artagon.xacml.v3.spi.PolicyReferenceResolver;
import com.artagon.xacml.v3.spi.xpath.JDKXPathProvider;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected EvaluationContextFactory contextFactory;
	protected PolicyReferenceResolver policyResolver;
	protected Policy currentPolicy;
	protected Request currentRequest;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.policyResolver = createStrictMock(PolicyReferenceResolver.class);
		this.currentRequest = createStrictMock(Request.class);
		this.currentPolicy = createStrictMock(Policy.class);
		this.contextFactory = new DefaultEvaluationContextFactory(policyResolver, 
				new JDKXPathProvider());
		this.context = contextFactory.createContext(currentPolicy, currentRequest);
	}
}