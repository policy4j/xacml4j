package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.AttributeAssignment;
import com.artagon.xacml.v3.policy.AttributeAssignmentExpression;
import com.artagon.xacml.v3.policy.Effect;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.PolicyElement;

abstract class BaseDecisionResponseExpression extends XacmlObject implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(BaseDecisionResponseExpression.class);
	
	private String id;
	private Effect effect;
	private Collection<AttributeAssignmentExpression> attributeExpressions;
	
	/**
	 * Constructs expression with a given identifier,
	 * effect and collection of {@link AttributeAssignmentExpression}
	 * expressions
	 *  
	 * @param id an identifier
	 * @param effect an effect
	 * @param attributeExpressions a collection of {@link AttributeAssignmentExpression}
	 */
	public BaseDecisionResponseExpression(String id, 
			Effect effect, 
			Collection<AttributeAssignmentExpression> attributeExpressions){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(effect);
		Preconditions.checkNotNull(attributeExpressions);
		this.id = id;
		this.effect = effect;
		this.attributeExpressions = new LinkedList<AttributeAssignmentExpression>(attributeExpressions);
	}
	
	/**
	 * Unique identifier
	 * 
	 * @return an identifier
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * Gets {@link Effect} instance
	 * 
	 * @return {@link Effect} instance
	 */
	public Effect getEffect(){
		return effect;
	}
	
	/**
	 * Tests if this decision info expression
	 * is applicable for a given {@link Decision}
	 * 
	 * @param result a decision result
	 * @return <code>true</code> if an expression is applicable
	 */
	public boolean isApplicable(Decision result){
		return (result == Decision.PERMIT && effect == Effect.PERMIT) ||
		(result == Decision.DENY && effect == Effect.DENY);
	}
	
	public Collection<AttributeAssignmentExpression> getAttributeAssignmentExpressions(){
		return Collections.unmodifiableCollection(attributeExpressions);
	}
	
	/**
	 * Evaluates collection of {@link AttributeAssignmentExpression} instances
	 * and return collection of {@link AttributeAssignment} instances
	 * @param context an evaluation context
	 * @return collection of {@link AttributeAssignment} instances
	 * @throws EvaluationException if an evaluation error occurs
	 */
	protected Collection<AttributeAssignment> evaluateAttributeAssingments(EvaluationContext context) throws EvaluationException
	{
		Collection<AttributeAssignment> attr = new LinkedList<AttributeAssignment>();
		for(AttributeAssignmentExpression attrExp : attributeExpressions){
			if(log.isDebugEnabled()){
				log.debug("Evaluating attribute assingment " +
						"expression attributeId=\"{}\" category=\"{}\" issuer=\"{}\"",
				new Object[]{attrExp.getAttributeId(), 
						attrExp.getCategory(), attrExp.getIssuer()});
			}
			attr.add(new AttributeAssignment(
					attrExp.getAttributeId(), 
					attrExp.getCategory(), 
					attrExp.getIssuer(), 
					attrExp.evaluate(context)));
		}
		return attr;
	}
}
