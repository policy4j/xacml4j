package com.artagon.xacml.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;

abstract class BaseDesicion implements Decision
{
	private final static Logger log = LoggerFactory.getLogger(BaseDesicion.class);
	
	private String id;
	private Target target;
	private Collection<AdviceExpression> adviceExpressions;
	private Collection<ObligationExpression> obligationExpressions;
	
	/**
	 * Constructs base decision with a given identifier
	 * 
	 * @param id an decision identifier
	 * @param target a a decision target
	 * @param adviceExpressions a decision 
	 * advice expressions
	 * @param obligationExpressions a decision 
	 * obligation expressions
	 */
	protected BaseDesicion(String id, 
			Target target, Collection<AdviceExpression> adviceExpressions,
			Collection<ObligationExpression> obligationExpressions){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(adviceExpressions);
		Preconditions.checkNotNull(obligationExpressions);
		this.id = id;
		this.target = target;
		this.adviceExpressions = new LinkedList<AdviceExpression>(adviceExpressions);
		this.obligationExpressions = new LinkedList<ObligationExpression>(obligationExpressions);
	}
	
	/**
	 * Constructs base decision with a given
	 * identifier and target
	 * 
	 * @param id a decision identifier
	 * @param target a decision target
	 */
	protected BaseDesicion(String id, 
			Target target){
		this(id, target, 
				Collections.<AdviceExpression>emptyList(),
				Collections.<ObligationExpression>emptyList());
	}
	
	@Override
	public final String getId(){
		return id;
	}
	
	@Override
	public final Target getTarget(){
		return target;
	}
	
	@Override
	public final Collection<ObligationExpression> getObligationExpressions(){
		return Collections.unmodifiableCollection(obligationExpressions);
	}
	
	@Override
	public final Collection<AdviceExpression> getAdviceExpressions(){
		return Collections.unmodifiableCollection(adviceExpressions);
	}
	
	@Override
	public MatchResult isApplicable(EvaluationContext context){ 
		Preconditions.checkArgument(isEvaluationContextValid(context));
		return (target == null)?MatchResult.MATCH:target.match(context);
	}
	
	/**
	 * Combines {@link #isApplicable(EvaluationContext)} and 
	 * {@link #evaluate(EvaluationContext)} calls to one single
	 * method invocation
	 */
	@Override
	public final DecisionResult evaluateIfApplicable(EvaluationContext context)
	{
		Preconditions.checkArgument(isEvaluationContextValid(context));
		MatchResult r = isApplicable(context);
		if(r == MatchResult.MATCH){
			return evaluate(context);
		}
		return (r == MatchResult.INDETERMINATE)?
				DecisionResult.INDETERMINATE:DecisionResult.NOT_APPLICABLE;
	}
	
	/**
	 * Evaluates this decision, if decision is applicable to
	 * the current request then appropriate decision
	 * advice and obligations are evaluated
	 */
	@Override
	public final DecisionResult evaluate(EvaluationContext context) 
	{
		Preconditions.checkArgument(isEvaluationContextValid(context));
		DecisionResult result = doEvaluate(context);
		if(result.isIndeterminate() || 
				result == DecisionResult.NOT_APPLICABLE){
			log.debug("Not evaluating advices and " +
					"obligations for decision result=\"{}\"", result);
			return result;
		}
		try
		{
			log.debug("Evaluating advice for decision with id=\"{}\"", getId());
			context.addAdvices(evaluateAdvices(context, result));
			log.debug("Evaluating obligations for decision with id=\"{}\"", getId());
			context.addObligations(evaluateObligations(context, result));
			return result;
		}catch(PolicyEvaluationException e){
			log.debug("Failed to evaluate decision id=\"{}\" " +
					"obligation or advice expressions", getId());
			return DecisionResult.INDETERMINATE;
		}
	}
	
	/**
	 * Evaluates advice expressions matching given decision
	 * {@link DecisionResult} result
	 * 
	 * @param context an evaluation context
	 * @param result a decision evaluation result
	 * @return collection of {@link Advice} instances
	 * @throws PolicyEvaluationException if an evaluation error occurs
	 */
	private Collection<Advice> evaluateAdvices(EvaluationContext context, DecisionResult result) 
		throws PolicyEvaluationException
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
	 * {@link DecisionResult} result
	 * 
	 * @param context an evaluation context
	 * @param result an decision result
	 * @return collection of {@link Obligation} instances
	 * @throws PolicyEvaluationException if an evaluation error occurs
	 */
	private Collection<Obligation> evaluateObligations(EvaluationContext context, DecisionResult result) 
		throws PolicyEvaluationException
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
	 * @return {@link DecisionResult}
	 */
	protected abstract DecisionResult doEvaluate(EvaluationContext context);
}
