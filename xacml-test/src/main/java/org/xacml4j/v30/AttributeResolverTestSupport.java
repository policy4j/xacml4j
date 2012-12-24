package org.xacml4j.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.xacml4j.v30.AttributeExpType;
import org.xacml4j.v30.BagOfAttributeExp;

public class AttributeResolverTestSupport {

	protected <T extends AttributeExpType> void checkHasAttribute(
			Map<String, BagOfAttributeExp> v, String attributeName,
			T type, Object ...values) {
		BagOfAttributeExp bag = v.get(attributeName);
		checkHasValues(bag, type, values);
	}

	protected <T extends AttributeExpType> void checkHasValues(BagOfAttributeExp bag,
			T type, Object ...values) {
		assertNotNull(bag);
		assertEquals(values.length, bag.size());
		for(Object value: values) {
			assertTrue(bag.contains(type.create(value)));
		}
	}
}
