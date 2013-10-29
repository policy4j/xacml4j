package org.xacml4j.v30.pdp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.Decision;


public class DecisionTest
{
	@Test
	public void testIndeterminate()
	{
		assertTrue(Decision.INDETERMINATE_D.isIndeterminate());
		assertTrue(Decision.INDETERMINATE_P.isIndeterminate());
		assertTrue(Decision.INDETERMINATE_DP.isIndeterminate());
		assertTrue(Decision.INDETERMINATE.isIndeterminate());
		assertFalse(Decision.DENY.isIndeterminate());
		assertFalse(Decision.PERMIT.isIndeterminate());
		assertFalse(Decision.NOT_APPLICABLE.isIndeterminate());

	}

}
