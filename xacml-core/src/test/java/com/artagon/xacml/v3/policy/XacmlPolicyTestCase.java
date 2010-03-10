package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;

import org.junit.Before;

import com.artagon.xacml.v3.policy.impl.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.policy.impl.JDKXPathProvider;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected EvaluationContextFactory contextFactory;
	protected AttributeResolver attributeService;
	protected PolicyResolver policyResolver;
	protected Policy currentPolicy;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.attributeService = createStrictMock(AttributeResolver.class);
		this.policyResolver = createStrictMock(PolicyResolver.class);
		this.currentPolicy = createStrictMock(Policy.class);
		this.contextFactory = new DefaultEvaluationContextFactory(attributeService, policyResolver, 
				new JDKXPathProvider());
		this.context = contextFactory.createContext(currentPolicy);
	}
}