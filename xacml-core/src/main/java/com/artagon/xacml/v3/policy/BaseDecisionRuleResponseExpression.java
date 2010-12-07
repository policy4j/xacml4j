package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.context.StatusCode;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * A base class for XACML Obligation or Advice expressions
 * 
 * @author Giedrius Trumpickas
 */
abstract class BaseDecisionRuleResponseExpression extends XacmlObject implements PolicyElement
{
	private String id;
	private Effect effect;
	private Multimap<String, AttributeAssignmentExpression> attributeExpressions;
	
	/**
	 * Constructs expression with a given identifier,
	 * effect and collection of {@link AttributeAssignmentExpression}
	 * expressions
	 *  
	 * @param id an identifier
	 * @param effect an effect
	 * @param attributeExpressions a collection of {@link AttributeAssignmentExpression}
	 */
	public BaseDecisionRuleResponseExpression(
			String id, 
			Effect effect, 
			Collection<AttributeAssignmentExpression> attributeExpressions) 
	{
		checkNotNull(id, "Decision rule expression id can not be null");
		checkNotNull(effect, "Decision rule expression effect can not be null");
		checkNotNull(attributeExpressions, 
				"Decision rule expression attribute expressions can not be null");
		this.id = id;
		this.effect = effect;
		this.attributeExpressions = LinkedHashMultimap.create();
		for(AttributeAssignmentExpression exp : attributeExpressions){
			Preconditions.checkArgument(exp != null, 
					"Decision rule response expression with id=\"%s\" " +
					"attribute assignment expression can not be null", id);
			this.attributeExpressions.put(exp.getAttributeId(), exp);
		}
	}
	
	/**
	 * Gets decision rule response 
	 * unique identifier
	 * 
	 * @return an unique identifier
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
		return Collections.unmodifiableCollection(attributeExpressions.values());
	}
	
	/**
	 * Evaluates collection of {@link AttributeAssignmentExpression} instances
	 * and return collection of {@link AttributeAssignment} instances
	 * @param context an evaluation context
	 * @return collection of {@link AttributeAssignment} instances
	 * @throws EvaluationException if an evaluation error occurs
	 */
	protected Collection<AttributeAssignment> evaluateAttributeAssingments(
			EvaluationContext context) 
		throws EvaluationException
	{
		try{
			Collection<AttributeAssignment> attr = new LinkedList<AttributeAssignment>();
			for(AttributeAssignmentExpression attrExp : attributeExpressions.values())
			{
				attr.add(new AttributeAssignment(
						attrExp.getAttributeId(), 
						attrExp.getCategory(), 
						attrExp.getIssuer(), 
						attrExp.evaluate(context)));
			}
			return attr;
		}catch(XacmlSyntaxException e){
			throw new EvaluationException(
					StatusCode.createProcessingError(), context, e);
		}
		
	}
}
