
package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.pdp.Rule;

public final class PermitOverridesRuleOrderedCombingingAlgorithm extends PermitOverrides<Rule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides";

	public PermitOverridesRuleOrderedCombingingAlgorithm() {
		super(ID);
	}
}
