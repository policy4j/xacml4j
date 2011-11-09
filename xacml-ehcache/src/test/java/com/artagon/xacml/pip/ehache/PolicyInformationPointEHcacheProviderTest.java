package com.artagon.xacml.pip.ehache;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v30.spi.pip.AttributeResolverDescriptorBuilder;
import com.artagon.xacml.v30.spi.pip.AttributeSet;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPointCacheProvider;
import com.artagon.xacml.v30.spi.pip.ResolverContext;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.ImmutableList;

@ContextConfiguration(locations={"classpath:/testContext.xml"})
public class PolicyInformationPointEHcacheProviderTest extends AbstractJUnit4SpringContextTests
{
	@Autowired
	private PolicyInformationPointCacheProvider pipCache;
	
	private AttributeResolverDescriptor attrDesc;
	private IMocksControl c;
	private ResolverContext context;
	
	@Before
	public void init(){
		this.attrDesc = AttributeResolverDescriptorBuilder
		.builder("testId1", "Test", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testAttr1", StringType.STRING)
		.attribute("testAttr2", IntegerType.INTEGER)
		.cache(20)
		.build();
		this.c = createControl();
		this.context = c.createMock(ResolverContext.class);
		
	}
	
	@Test
	public void test()
	{
		expect(context.getDescriptor()).andReturn(attrDesc);
		expect(context.getKeys()).andReturn(ImmutableList.of(StringType.STRING.bagOf("v1")));
		expect(context.getDescriptor()).andReturn(attrDesc);
		expect(context.getKeys()).andReturn(ImmutableList.of(StringType.STRING.bagOf("v1")));
		c.replay();
		Map<String, BagOfAttributeExp> v = new HashMap<String, BagOfAttributeExp>();
		v.put("testAttr1", StringType.STRING.bagOf("v1"));
		v.put("testAttr2", IntegerType.INTEGER.bagOf(1, 2, 3));
		AttributeSet set1 = new AttributeSet(attrDesc, v);
		pipCache.putAttributes(context, set1);
		AttributeSet set2 = pipCache.getAttributes(context);
		assertNotNull(set2);
		assertEquals(set1.getDescriptor().getId(), set2.getDescriptor().getId());
		c.verify();
	}
}
