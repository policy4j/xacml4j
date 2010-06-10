package com.artagon.xacml.v3;

import java.util.Collection;

import com.google.common.base.Preconditions;

abstract class BaseCompositeDecisionRule extends BaseDesicionRule 
	implements CompositeDecisionRule, Versionable
{
	private PolicyIdentifier identifier;
	
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
		super(description, target,  adviceExpressions, obligationExpressions);
		Preconditions.checkNotNull(version);
		this.identifier = new PolicyIdentifier(id, version);
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
	public String getId(){
		return identifier.getId();
	}
	
	@Override
	public  Version getVersion() {
		return identifier.getVersion();
	}
	
	public PolicyIdentifier getPolicyIdentifier(){
		return identifier;
	}
}
