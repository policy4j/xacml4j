package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;

import org.junit.Before;

import com.artagon.xacml.v3.policy.impl.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.policy.spi.xpath.JDKXPathProvider;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected EvaluationContextFactory contextFactory;
	protected AttributeReferenceResolver attributeService;
	protected DecisionRuleReferenceResolver policyResolver;
	protected Policy currentPolicy;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.attributeService = createStrictMock(AttributeReferenceResolver.class);
		this.policyResolver = createStrictMock(DecisionRuleReferenceResolver.class);
		this.currentPolicy = createStrictMock(Policy.class);
		this.contextFactory = new DefaultEvaluationContextFactory(attributeService, policyResolver, 
				new JDKXPathProvider());
		this.context = contextFactory.createContext(currentPolicy);
	}
}