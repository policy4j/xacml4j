package org.xacml4j.v30.policy.combine;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithms.evaluateIfMatch;

import java.util.List;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;


/**
 * Implementation of the {@code urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second}
 * combining algorithm as per <a href="http://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/xacml-3.0-combalgs-v1.0.html">XACML 3.0 Additional Combining Algorithms Profile Version 1.0</a>
 * {@code [xacml-3.0-combalgs]}.
 *
 * @author Giedrius Trumpickas
 * @author Valdas Sevelis
 */
public class OnPermitApplySecondPolicyCombiningAlgorithm
		extends BaseDecisionCombiningAlgorithm<CompositeDecisionRule> {

	private static final String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second";

	private static final Status PROCESSING_ERROR = Status.processingError().build();

	public OnPermitApplySecondPolicyCombiningAlgorithm() {
		super(ID);
	}

	@Override
	public Decision combine(EvaluationContext context,
	                        List<CompositeDecisionRule> policies) {
		final int numberOfPolicies = policies.size();
		if (numberOfPolicies < 2 || numberOfPolicies > 3) {
			context.setEvaluationStatus(PROCESSING_ERROR);
			return Decision.INDETERMINATE_DP;
		}
		final Decision decision0 = evaluateIfMatch(context, policies.get(0));
		if (decision0 == Decision.NOT_APPLICABLE ||
				decision0 == Decision.DENY ||
				decision0 == Decision.INDETERMINATE_D) {
			if (numberOfPolicies == 2) {
				return Decision.NOT_APPLICABLE;
			} else {
				return evaluateIfMatch(context, policies.get(2));
			}
		}
		if (decision0 == Decision.PERMIT) {
			return evaluateIfMatch(context, policies.get(1));
		}

		// decision0 is Indeterminate{P} or Indeterminate{DP}
		// Use status code of decision0
		context.setEvaluationStatus(PROCESSING_ERROR);
		return Decision.INDETERMINATE_DP;
	}
}
