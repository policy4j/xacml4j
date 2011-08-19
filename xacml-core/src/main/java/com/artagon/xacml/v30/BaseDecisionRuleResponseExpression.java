package com.artagon.xacml.v30;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * A base class for XACML Obligation or Advice expressions
 * 
 * @author Giedrius Trumpickas
 */
abstract class BaseDecisionRuleResponseExpression implements PolicyElement
{
	protected String id;
	protected Effect effect;
	protected Multimap<String, AttributeAssignmentExpression> attributeExpressions;
	
	private int hashCode;
	
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
		Preconditions.checkNotNull(id, "" +
				"Decision rule expression id can not be null");
		Preconditions.checkNotNull(effect, 
				"Decision rule expression effect can not be null");
		Preconditions.checkNotNull(attributeExpressions, 
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
		this.attributeExpressions = Multimaps.unmodifiableMultimap(this.attributeExpressions);
		this.hashCode = Objects.hashCode(id, effect, attributeExpressions);
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
	
		Collection<AttributeAssignment> attr = new LinkedList<AttributeAssignment>();
		for(AttributeAssignmentExpression attrExp : attributeExpressions.values())
		{
			ValueExpression val = attrExp.evaluate(context);
			if(val instanceof AttributeValue){
				attr.add(new AttributeAssignment(
						attrExp.getAttributeId(), 
						attrExp.getCategory(), 
						attrExp.getIssuer(), 
						(AttributeValue)val));
				continue;
			}
			BagOfAttributeValues bag = (BagOfAttributeValues)val;
			for(AttributeValue v : bag.values()){
				attr.add(new AttributeAssignment(
						attrExp.getAttributeId(), 
						attrExp.getCategory(), 
						attrExp.getIssuer(), 
						v));
			}
		}
		return attr;
	}
	
	@Override
	public int hashCode(){
		return hashCode;
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("id", id)
		.add("effect", effect)
		.add("expressions", attributeExpressions)
		.toString();
	}
}
