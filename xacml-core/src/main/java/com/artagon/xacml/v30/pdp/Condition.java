package com.artagon.xacml.v30.pdp;

import static com.artagon.xacml.v30.types.BooleanType.BOOLEAN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v30.types.BooleanExp;
import com.artagon.xacml.v30.types.DataTypes;

/**
 * Condition represents a Boolean expression that refines the applicability 
 * of the rule beyond the predicates implied by its target. 
 * Therefore, it may be absent in the {@link Rule}
 * 
 * @author Giedrius Trumpickas
 */
public class Condition extends XacmlObject implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(Condition.class);
	
	private Expression predicate;

	/**
	 * Constructs condition with an predicate
	 * expression
	 * 
	 * @param predicate an expression which always evaluates
	 * to {@link BooleanExp}
	 */
	public Condition(Expression predicate) 
	{
		checkNotNull(predicate, "Condition predicate can not be null");
		checkArgument(predicate.getEvaluatesTo().equals(BOOLEAN),
				"Condition expects an expression " +
					"with=\"%s\" return value, but got expression " +
					"with return value type=\"%s\"", 
					DataTypes.BOOLEAN, predicate.getEvaluatesTo());
		this.predicate = predicate;
	}
	
	/**
	 * Gets condition expression predicate
	 * 
	 * @return {@link Expression} a condition 
	 * expression predicate
	 */
	public Expression getExpression(){
		return predicate;
	}
	/**
	 * Evaluates this condition and returns instance of
	 * {@link ConditionResult}
	 * 
	 * @param context an evaluation context
	 * @return {@link ConditionResult}
	 */
	public ConditionResult evaluate(EvaluationContext context) 
	{
		try
		{
			BooleanExp result = (BooleanExp)predicate.evaluate(context);
			return result.getValue()?ConditionResult.TRUE:ConditionResult.FALSE;
		}catch(EvaluationException e){
			if(log.isDebugEnabled()){
				log.debug("Failed to evaluate condition", e);
			}
			context.setEvaluationStatus(e.getStatusCode());
			return ConditionResult.INDETERMINATE;
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug("Failed to evaluate condition", e);
			}
			context.setEvaluationStatus(StatusCode.createProcessingError());
			return ConditionResult.INDETERMINATE;
		}
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
