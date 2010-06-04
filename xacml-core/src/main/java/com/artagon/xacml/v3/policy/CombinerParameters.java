package com.artagon.xacml.v3.policy;


public class CombinerParameters extends BaseDecisionCombinerParameters 
	implements PolicyElement
{
	public CombinerParameters(
			Iterable<CombinerParameter> parameters) {
		super(parameters);
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(CombinerParameter p : parameters.values()){
			p.accept(v);
		}
		v.visitLeave(this);
	}
}
