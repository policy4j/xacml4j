package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.AdviceExpression;
import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.ObligationExpression;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.Versionable;
import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRule extends BaseDesicionRule 
	implements CompositeDecisionRule, Versionable
{
	
	private Version version;
	
	/**
	 * Constructs composite decision rule
	 * 
	 * @param id a rule identifier
	 * @param target a rule target
	 * @param adviceExpressions a rule advice 
	 * expressions
	 * @param obligationExpressions a rule obligation 
	 * expressions
	 */
	protected BaseCompositeDecisionRule(
			String id, 
			Version version,
			Target target,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(id, target, adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(version);
		this.version = version;
	}
	
	@Override
	public final Version getVersion() {
		return version;
	}
}
