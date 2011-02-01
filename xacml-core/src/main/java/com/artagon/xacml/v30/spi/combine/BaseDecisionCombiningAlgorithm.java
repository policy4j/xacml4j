package com.artagon.xacml.v30.spi.combine;

import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.XacmlObject;
import com.google.common.base.Preconditions;

public abstract class BaseDecisionCombiningAlgorithm <D extends DecisionRule> extends XacmlObject
	implements DecisionCombiningAlgorithm <D>
{
	private String algorithmId;
	
	/**
	 * Creates decision combining algorithm with a
	 * given algorithm identifier
	 * @param algorithmId an algorithm identifier
	 */
	protected BaseDecisionCombiningAlgorithm(String algorithmId){
		Preconditions.checkNotNull(algorithmId);
		this.algorithmId = algorithmId;
	}
	
	/**
	 * Gets decision algorithm identifier
	 * 
	 * @return decision algorithm identifier
	 */
	public final String getId(){
		return algorithmId;
	}
}
