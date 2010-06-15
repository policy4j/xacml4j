package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

abstract class BaseDesicionRule extends XacmlObject implements DecisionRule
{
	private final static Logger log = LoggerFactory.getLogger(BaseDesicionRule.class);
	
	private String description;
	private Target target;
	private Collection<AdviceExpression> adviceExpressions;
	private Collection<ObligationExpression> obligationExpressions;
	
	/**
	 * Constructs base decision with a given identifier
	 * 
	 * @param id an decision identifier
	 * @param description a decision rule description
	 * @param target a a decision target
	 * @param adviceExpressions a decision 
	 * advice expressions
	 * @param obligationExpressions a decision 
	 * obligation expressions
	 */
	protected BaseDesicionRule( 
			String description,
			Target target, Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		this.description = description;
		this.target = target;
		this.adviceExpressions = (adviceExpressions == null)?
				Collections.<AdviceExpression>emptyList():
					new LinkedList<AdviceExpression>(adviceExpressions);
		this.obligationExpressions = (obligationExpressions == null)?
				Collections.<ObligationExpression>emptyList():
					new LinkedList<ObligationExpression>(obligationExpressions);
	}
	
	protected BaseDesicionRule(
			Target target, Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		this(null, target, adviceExpressions, obligationExpressions);
	}
	
	/**
	 * Constructs base decision with a given
	 * identifier and target
	 * 
	 * @param id a decision identifier
	 * @param target a decision target
	 */
	protected BaseDesicionRule(String id, 
			Target target){
		this(id, target, 
				Collections.<AdviceExpression>emptyList(),
				Collections.<ObligationExpression>emptyList());
	}
	
	/**
	 * Gets decision rule description
	 * 
	 * @return decision rule description
	 */
	public String getDescription(){
		return description;
	}
	
	/**
	 * Gets decision rule target
	 * 
	 * @return {@link Target} or <code>null</code>
	 * if rule is applicable to any request
	 */
	public Target getTarget(){
		return target;
	}
	
	/**
	 * Gets decision rule collection of {@link ObligationExpression}
	 * expressions
	 * 
	 * @return a collection of {@link ObligationExpression}
	 */
	public Collection<ObligationExpression> getObligationExpressions(){
		return Collections.unmodifiableCollection(obligationExpressions);
	}
	
	/**
	 * Gets decision rule collection of {@link AdviceExpression}
	 * expressions
	 * 
	 * @return a collection of {@link AdviceExpression}
	 */
	public Collection<AdviceExpression> getAdviceExpressions(){
		return Collections.unmodifiableCollection(adviceExpressions);
	}
	
	@Override
	public MatchResult isApplicable(EvaluationContext context){ 
		Preconditions.checkArgument(isEvaluationContextValid(context));
		return (target == null)?MatchResult.MATCH:target.match(context);
	}
	
	/**
	 * Evaluates this decision, if decision is applicable to
	 * the current request then appropriate decision
	 * advice and obligations are evaluated
	 */
	@Override
	public  Decision evaluate(EvaluationContext context) 
	{
		if(log.isDebugEnabled()){
			log.debug("Invoking decision rule id=\"{}\" evaluate", getId());
		}
		Preconditions.checkArgument(isEvaluationContextValid(context));
		Decision result = doEvaluate(context);
		if(result.isIndeterminate() || 
				result == Decision.NOT_APPLICABLE){
			log.debug("Not evaluating advices and " +
					"obligations for decision rule id=\"{}\" " +
					"evaluation result=\"{}\"", getId(), result);
			return result;
		}
		try
		{
			log.debug("Evaluating advice for decision with id=\"{}\"", getId());
			context.addAdvices(evaluateAdvices(context, result));
			log.debug("Evaluating obligations for decision with id=\"{}\"", getId());
			context.addObligations(evaluateObligations(context, result));
			return result;
		}catch(EvaluationException e){
			log.debug("Failed to evaluate decision id=\"{}\" " +
					"obligation or advice expressions", getId());
			return Decision.INDETERMINATE;
		}
	}
	
	/**
	 * Evaluates advice expressions matching given decision
	 * {@link Decision} result
	 * 
	 * @param context an evaluation context
	 * @param result a decision evaluation result
	 * @return collection of {@link Advice} instances
	 * @throws EvaluationException if an evaluation error occurs
	 */
	private Collection<Advice> evaluateAdvices(EvaluationContext context, Decision result) 
		throws EvaluationException
	{
		Collection<Advice> advices = new LinkedList<Advice>();
		for(AdviceExpression adviceExp : adviceExpressions){
			if(adviceExp.isApplicable(result)){
				log.debug("Evaluating advice id=\"{}\" for decision=\"{}\"", 
						adviceExp.getId(), result);
				advices.add(adviceExp.evaluate(context));
			}
		}
		log.debug("Evaluated \"{}\" applicable advices", advices.size());
		return advices;
	}
	
	/**
	 * Evaluates obligation matching given decision 
	 * {@link Decision} result
	 * 
	 * @param context an evaluation context
	 * @param result an decision result
	 * @return collection of {@link Obligation} instances
	 * @throws EvaluationException if an evaluation error occurs
	 */
	private Collection<Obligation> evaluateObligations(EvaluationContext context, Decision result) 
		throws EvaluationException
	{
		Collection<Obligation> obligations = new LinkedList<Obligation>();
		for(ObligationExpression obligationExp : obligationExpressions){
			if(obligationExp.isApplicable(result)){
				log.debug("Evaluating obligation id=\"{}\" for decision=\"{}\"", 
						obligationExp.getId(), result);
				obligations.add(obligationExp.evaluate(context));
			}
		}
		log.debug("Evaluated \"{}\" applicable obligations", obligations.size());
		return obligations;
	}
	
	/**
	 * Checks if given evaluation context can be used to
	 * evaluate this decision
	 * 
	 * @param context an evaluation context
	 * @return <code>true></code> if this evaluation context
	 * can be used to evaluate this decision
	 */
	protected abstract boolean isEvaluationContextValid(EvaluationContext context);
	
	/**
	 * Performs actual decision evaluation
	 * 
	 * @param context an evaluation context
	 * @return {@link Decision}
	 */
	protected abstract Decision doEvaluate(EvaluationContext context);
}
