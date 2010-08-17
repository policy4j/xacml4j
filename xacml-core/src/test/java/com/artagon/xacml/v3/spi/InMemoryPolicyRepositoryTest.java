package com.artagon.xacml.v3.spi;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;

public class InMemoryPolicyRepositoryTest 
{
	private Policy p1v1;
	private Policy p1v2;
	private Policy p1v3;
	
	private PolicyRepository r;
	
	@Before
	public void init(){
		this.p1v1 = createStrictMock(Policy.class);
		this.p1v2 = createStrictMock(Policy.class);
		this.p1v3 = createStrictMock(Policy.class);
		this.r = new InMemoryPolicyRepository();
	}
	
	@Test
	public void addPolicyTest() throws Exception
	{
		
		expect(p1v2.getId()).andReturn("id1");
		expect(p1v2.getVersion()).andReturn(Version.parse("1.1"));
		
		expect(p1v1.getId()).andReturn("id1");
		expect(p1v1.getVersion()).andReturn(Version.parse("1"));
		
		
		expect(p1v3.getId()).andReturn("id1");
		expect(p1v3.getVersion()).andReturn(Version.parse("1.2.1"));
		
		
		expect(p1v2.getVersion()).andReturn(Version.parse("1.1"));
		expect(p1v1.getVersion()).andReturn(Version.parse("1"));
		expect(p1v3.getVersion()).andReturn(Version.parse("1.2.1"));
		
		
		replay(p1v1, p1v2, p1v3);
		
		r.add(p1v2);
		r.add(p1v1);
		r.add(p1v3);
		
		Collection<Policy> found = r.getPolicies("id1", VersionMatch.parse("1.+"));
		assertEquals(3, found.size());
		Iterator<Policy> it = found.iterator();
		assertEquals(Version.parse("1"), it.next().getVersion());
//		assertEquals(Version.parse("1.1"), it.next().getVersion());
//		assertEquals(Version.parse("1.2.1"), it.next().getVersion());
		
		verify(p1v1, p1v2, p1v3);
	}
}
