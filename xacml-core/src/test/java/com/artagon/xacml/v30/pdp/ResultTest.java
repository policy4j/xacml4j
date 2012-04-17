package com.artagon.xacml.v30.pdp;

import org.easymock.IMocksControl;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.ImmutableList;

public class ResultTest 
{
	private IMocksControl c;
	
	private Obligation denyObligationWithId1;
	private Obligation denyObligationWithId2;
	private Obligation denyObligationWithSameId1;
	
	private Obligation permitObligationWithId1;
	private Obligation permitObligationWithSameId1;
	private Obligation permitObligationWithId2;
	
	
	@Before
	public void init(){
		c = createControl();
		this.denyObligationWithId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v1"))
				.create();
		this.denyObligationWithId2 = Obligation
				.builder("id2", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v1"))
				.create();
		this.denyObligationWithSameId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", StringType.STRING.create("v2"))
				.create();
	}
	
	@Test
	public void testObligations()
	{
	}
}
