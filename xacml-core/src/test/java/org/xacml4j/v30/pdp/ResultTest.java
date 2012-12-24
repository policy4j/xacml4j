package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createControl;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.types.StringType;


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
	public void testObligations()
	{
	}
}
