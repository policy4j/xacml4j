package com.artagon.xacml.v3;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.type.DataTypes;

public class RequestProcessingPipelineTest 
{
	private PolicyDecisionPointCallback pdp;
	private Request request;
	private RequestProcessingPipeline pipeline;
	
	private RequestProcessingProfile p1;
	private RequestProcessingProfile p2;
	private RequestProcessingProfile p3;
	
	@Before
	public void init(){
		this.pdp = createStrictMock(PolicyDecisionPointCallback.class);
		this.p1 = createStrictMock(RequestProcessingProfile.class);
		this.p2 = createStrictMock(RequestProcessingProfile.class);
		this.p3 = createStrictMock(RequestProcessingProfile.class);
		this.pipeline = new RequestProcessingPipeline();
		
		Collection<Attribute> attributes0 = new LinkedList<Attribute>();
		attributes0.add(new Attribute("testId1", DataTypes.STRING.create("value0")));
		attributes0.add(new Attribute("testId2", DataTypes.STRING.create("value1")));
		Attributes attr0 = new Attributes("resourceAttr0",  AttributeCategoryId.RESOURCE, attributes0);
		
		Collection<Attribute> attributes1 = new LinkedList<Attribute>();
		attributes1.add(new Attribute("testId5", DataTypes.STRING.create("value0")));
		attributes1.add(new Attribute("testId6", DataTypes.STRING.create("value1")));
		Attributes attr1 = new Attributes("subjectAttr0",  AttributeCategoryId.SUBJECT_ACCESS, attributes1);
		this.request = new Request(false, Arrays.asList(attr0, attr1));
		
	}
	
	@Test
	public void testPipelineWithNoProfiles()
	{
		expect(pdp.decide(request)).andReturn(new Result(Decision.DENY));
		replay(pdp);
		Collection<Result> results = pipeline.process(request, pdp);
		assertEquals(1, results.size());
		assertEquals(new Result(Decision.DENY), results.iterator().next());
		verify(pdp);
	}
	
	@Test
	public void testPipelineWithSingleProfile()
	{
		Capture<RequestProcessingCallback> c = new Capture<RequestProcessingCallback>();
		pipeline.addProfile(p1);
		expect(p1.process(eq(request) , capture(c))).andAnswer(new IAnswer<Collection<Result>>() {
			@Override
			public Collection<Result> answer() throws Throwable {
				Object[] args = getCurrentArguments();
				RequestProcessingCallback cb = (RequestProcessingCallback)args[1];
				return cb.invokeNext(request);
			}
			
		});
		expect(pdp.decide(request)).andReturn(new Result(Decision.DENY));
		replay(p1, pdp);
		Collection<Result> results = pipeline.process(request, pdp);
		assertEquals(1, results.size());
		assertEquals(new Result(Decision.DENY), results.iterator().next());
		verify(p1, pdp);
	}
}
