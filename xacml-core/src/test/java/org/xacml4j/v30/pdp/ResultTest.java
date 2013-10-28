package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createControl;
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.StringType;

import com.google.common.collect.ImmutableList;


public class ResultTest 
{
	private IMocksControl c;
	
	private Obligation denyObligationWithId1;
	private Obligation denyObligationWithId2;
	private Obligation denyObligationWithSameId1;	
	
	@Before
	public void init(){
		c = createControl();
		this.denyObligationWithId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v1"))
				.build();
		this.denyObligationWithId2 = Obligation
				.builder("id2", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v1"))
				.build();
		this.denyObligationWithSameId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v2"))
				.build();
	}
	
	@Test
	public void testMergeObligationValues()
	{
		Result.Builder b = Result.builder(Decision.DENY, Status.createSuccess());
		b.obligation(denyObligationWithId1);
		b.obligation(ImmutableList.of(denyObligationWithId2, denyObligationWithSameId1));
		Result r = b.build();
		assertEquals(Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v1"))
				.attribute("test1", StringType.STRING.create("v2"))
				.build(), r.getObligation("id1"));
		assertEquals(Obligation
				.builder("id2", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v1"))
				.build(), r.getObligation("id2"));
		
	}
}
