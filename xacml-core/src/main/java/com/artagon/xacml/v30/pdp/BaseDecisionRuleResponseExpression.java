package com.artagon.xacml.v30.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.artagon.xacml.v30.AttributeAssignment;
import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeExp;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.ValueExpression;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;

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
	protected BaseDecisionRuleResponseExpression(BaseDecisionRuleResponseExpressionBuilder<?> b)
	{
		this.id = b.id;
		this.effect = b.effect;
		this.attributeExpressions = b.attributes.build();
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
			if(val instanceof AttributeExp){
				attr.add(new AttributeAssignment(
						attrExp.getAttributeId(),
						attrExp.getCategory(),
						attrExp.getIssuer(),
						(AttributeExp)val));
				continue;
			}
			BagOfAttributeExp bag = (BagOfAttributeExp)val;
			for(AttributeExp v : bag.values()){
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

	public static abstract class BaseDecisionRuleResponseExpressionBuilder <T extends BaseDecisionRuleResponseExpressionBuilder<?>>
	{
		private String id;
		private Effect effect;
		private ImmutableListMultimap.Builder<String, AttributeAssignmentExpression> attributes = ImmutableListMultimap.builder();

		public T effect(Effect effect){
			Preconditions.checkNotNull(effect);
			this.effect = effect;
			return getThis();
		}

		public T id(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
			this.id = id;
			return getThis();
		}

		public T attribute(String id, Expression exp){
			return attribute(AttributeAssignmentExpression.builder(id).expression(exp));
		}

		public T attribute(String id, AttributeCategory category, Expression exp){
			return attribute(AttributeAssignmentExpression.builder(id).category(category).expression(exp));
		}

		public T attribute(String id, AttributeCategory category, String issuer, Expression exp){
			return attribute(AttributeAssignmentExpression.builder(id).category(category).issuer(issuer).expression(exp));
		}

		public T attribute(AttributeAssignmentExpression ...attrs){
			for(AttributeAssignmentExpression a : attrs){
				attributes.put(a.getAttributeId(), a);
			}
			return getThis();
		}

		public T attribute(Iterable<AttributeAssignmentExpression> attrs){
			for(AttributeAssignmentExpression a : attrs){
				attributes.put(a.getAttributeId(), a);
			}
			return getThis();
		}

		public T attribute(
				AttributeAssignmentExpression.Builder b)
		{
			AttributeAssignmentExpression exp = b.build();
			attributes.put(exp.getAttributeId(), exp);
			return getThis();
		}

		protected abstract T getThis();

	}
}
