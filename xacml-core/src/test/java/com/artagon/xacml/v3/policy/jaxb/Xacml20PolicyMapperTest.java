package com.artagon.xacml.v3.policy.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.context.RequestType;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;

import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.MatchAnyOf;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.policy.impl.DefaultPolicyFactory;
import com.artagon.xacml.v3.policy.impl.combine.DefaultDecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.policy.spi.function.DefaultFunctionProvidersRegistry;

public class Xacml20PolicyMapperTest 
{
	private static JAXBContext context;
	private Xacml20PolicyMapper mapper;
	
	@BeforeClass
	public static void init_static() throws Exception
	{
		try{
			context = JAXBContext.newInstance("org.oasis.xacml.v20.policy:org.oasis.xacml.v20.context");
		}catch(JAXBException e){
			System.err.println(e.getMessage());
		}
	}
	
	@Before
	public void init() throws Exception
	{
		PolicyFactory policyFactory = new DefaultPolicyFactory(
				new DefaultFunctionProvidersRegistry(), new DefaultDecisionCombiningAlgorithmProvider());
		mapper = new Xacml20PolicyMapper(policyFactory);
	}
	
	@SuppressWarnings({"unchecked" })
	private static <T> T getPolicy(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("oasis-xacml20-compat-test/" + name);
		assertNotNull(stream);
		JAXBElement<T> policy = (JAXBElement<T>)context.createUnmarshaller().unmarshal(stream);
		assertNotNull(policy);
		return policy.getValue();
	}
	
	
	@Test
	public void testPolicyIIIF005Mapping() throws Exception
	{
		PolicyType policyF005 = getPolicy("IIIF005Policy.xml");
		Policy p0 = mapper.create(policyF005);
		assertEquals("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF005:policy", p0.getId());
		assertEquals("Policy for Conformance Test IIIF005.", p0.getDescription());
		assertNotNull(p0.getDefaults());
		assertEquals(XPathVersion.XPATH1, p0.getDefaults().getXPathVersion());
		assertNull(p0.getTarget());
		assertEquals(1, p0.getRules().size());
		Rule r = p0.getRules().get(0);
		assertEquals("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF005:rule", r.getId());
		assertEquals("Julius Hibbert can read or write Bart Simpson's medical record.", r.getDescription());
		assertEquals(Effect.PERMIT, r.getEffect());
		assertNotNull(r.getTarget());
		assertNull(r.getCondition());
		Target target = r.getTarget();
		assertEquals(3, target.getAnyOf().size());
		Iterator<MatchAnyOf> it = target.getAnyOf().iterator();
		MatchAnyOf m0 = it.next();
		MatchAnyOf m1 = it.next();
		MatchAnyOf m2 = it.next();
		assertEquals(2, m0.getAllOf().size());
		assertEquals(1, m1.getAllOf().size());
		assertEquals(1, m2.getAllOf().size());
	}
	
	@Test
	public void testPolicyIIIF006Mapping() throws Exception
	{
		PolicySetType policyF006 = getPolicy("IIIF006Policy.xml");
		PolicySet p0 = mapper.create(policyF006);
		assertNotNull(p0);
		assertEquals("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF006:policySet", p0.getId());
		assertEquals("Policy Set for Conformance Test IIIF006.", p0.getDescription());
		assertNotNull(p0.getDefaults());
		assertEquals(XPathVersion.XPATH1, p0.getDefaults().getXPathVersion());
		assertNotNull(p0.getTarget());
		assertEquals(1, p0.getDecisions().size());
	}
}
