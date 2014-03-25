package org.xacml4j.v30.pdp;

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;

public interface EvaluationContextHandler
{
	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeDesignatorKey key) throws EvaluationException;

	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeSelectorKey key) throws EvaluationException;
}
