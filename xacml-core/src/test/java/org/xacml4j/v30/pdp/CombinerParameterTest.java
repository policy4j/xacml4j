package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createStrictControl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.IMocksControl;
import org.junit.Test;
import org.xacml4j.v30.AttributeExp;

/**
 * @author Valdas Sevelis
 */
public class CombinerParameterTest {
	@Test
	public void testObjectMethods() {
		IMocksControl ctl = createStrictControl();
		AttributeExp exp1 = ctl.createMock(AttributeExp.class);
		AttributeExp exp2 = ctl.createMock(AttributeExp.class);

		CombinerParameter p1 = new CombinerParameter("name1", exp1);
		CombinerParameter p2 = new CombinerParameter("name1", exp1);
		CombinerParameter p3 = new CombinerParameter("name1", exp2);

		ctl.replay();
		assertTrue(p1.equals(p2));
		assertFalse(p1.equals(p3));
		assertFalse(p1.equals(exp1));
		assertFalse(p1.equals(null));

		assertEquals(p1.hashCode(), p2.hashCode());
		assertEquals(p1.toString(), p2.toString());
		ctl.verify();
	}
}
