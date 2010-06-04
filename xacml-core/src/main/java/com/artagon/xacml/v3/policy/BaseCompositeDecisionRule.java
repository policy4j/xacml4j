package com.artagon.xacml.v3.policy;

import java.util.Collection;

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
			String description,
			Target target,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		super(id, description, target,  adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(version);
		this.version = version;
	}
	
	protected BaseCompositeDecisionRule(
			String id, 
			Version version,
			Target target,
			Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		this(id, version, null, target, adviceExpressions, obligationExpressions);
	}
	
	@Override
	public  Version getVersion() {
		return version;
	}
}
