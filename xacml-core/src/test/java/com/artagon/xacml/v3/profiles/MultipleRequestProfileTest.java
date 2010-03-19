package com.artagon.xacml.v3.profiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.Attributes;
import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.RequestContextException;
import com.artagon.xacml.v3.RequestReference;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class MultipleRequestProfileTest 
{
	private RequestContextProfile profile;
	
	@Before
	public void init(){
		this.profile = new MultipleRequestsProfile();
	}
	
	@Test
	public void testResolveRequestsWithValidReferences() throws RequestContextException
	{
		Collection<Attribute> attributes0 = new LinkedList<Attribute>();
		attributes0.add(new Attribute("testId1", DataTypes.STRING.create("value0")));
		attributes0.add(new Attribute("testId2", DataTypes.STRING.create("value1")));
		Attributes attr0 = new Attributes("resourceAttr0",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes1 = new LinkedList<Attribute>();
		attributes1.add(new Attribute("testId3", DataTypes.STRING.create("value0")));
		attributes1.add(new Attribute("testId4", DataTypes.STRING.create("value1")));
		Attributes attr1 = new Attributes("resourceAttr1",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes2 = new LinkedList<Attribute>();
		attributes2.add(new Attribute("testId5", DataTypes.STRING.create("value0")));
		attributes2.add(new Attribute("testId6", DataTypes.STRING.create("value1")));
		Attributes attr2 = new Attributes("subjectAttr0",  AttributeCategoryId.SUBJECT_ACCESS, attributes0);
		
		Collection<Attribute> attributes3 = new LinkedList<Attribute>();
		attributes3.add(new Attribute("testId7", DataTypes.STRING.create("value0")));
		attributes3.add(new Attribute("testId8", DataTypes.STRING.create("value1")));
		Attributes attr3 = new Attributes("subjectAttr1",  AttributeCategoryId.SUBJECT_ACCESS, attributes0);
		
		
		RequestReference reference0 = new RequestReference(new AttributesReference("resourceAttr0"), new AttributesReference("subjectAttr0"));
		RequestReference reference1 = new RequestReference(new AttributesReference("resourceAttr1"), new AttributesReference("subjectAttr1"));
		
		RequestContext context = new RequestContext(false, 
				Arrays.asList(attr0, attr1, attr2, attr3), 
				Arrays.asList(reference0, reference1));
		Collection<RequestContext> requests = profile.process(context);
		Iterator<RequestContext> it = requests.iterator();
		assertEquals(2, requests.size());
		RequestContext context0 = it.next();
		RequestContext context1 = it.next();
		assertNotNull(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS, "testId5"));
		assertNotNull(context0.getAttributes(AttributeCategoryId.SUBJECT_ACCESS, "testId6"));
	}
}
