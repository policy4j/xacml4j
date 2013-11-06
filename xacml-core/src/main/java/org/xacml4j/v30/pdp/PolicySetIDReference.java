package org.xacml4j.v30.pdp;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyResolutionException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A XACML {@link PolicySet} reference
 *
 * @author Giedrius Trumpickas
 */
public final class PolicySetIDReference extends BaseCompositeDecisionRuleIDReference
	implements PolicyElement
{

	private PolicySetIDReference(Builder b) {
		super(b);
	}

	public static Builder builder(){
		return new Builder();
	}

	public static Builder builder(String id){
		return new Builder().id(id);
	}

	/**
	 * Test this reference points to a given policy
	 *
	 * @param policy a policy
	 * @return <code>true</code> if a this reference
	 * points to a given policies
	 */
	@Override
	public boolean isReferenceTo(CompositeDecisionRule policy) {
		PolicySet p = (PolicySet)policy;
		return p != null &&
		matches(p.getId(), p.getVersion());
	}

	/**
	 * Creates an {@link EvaluationContext} to evaluate this reference.
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		Preconditions.checkNotNull(context);
		if(context.getCurrentPolicySetIDReference() ==  this){
			return context;
		}
		PolicySetIDReferenceEvaluationContext refContext = new PolicySetIDReferenceEvaluationContext(context);
		try{
			CompositeDecisionRule policySet = refContext.resolve(this);
			if(policySet == null){
				return refContext;
			}
			return policySet.createContext(refContext);
		}catch(PolicyResolutionException e){
			return refContext;
		}
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return Decision.INDETERMINATE;
		}
		CompositeDecisionRule ps = context.getCurrentPolicySet();
		Preconditions.checkNotNull(ps);
		return ps.evaluate(context);
	}

	@Override
	public MatchResult isMatch(EvaluationContext context) {
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return MatchResult.INDETERMINATE;
		}
		CompositeDecisionRule ps = context.getCurrentPolicySet();
		Preconditions.checkNotNull(ps);
		return ps.isMatch(context);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public CompositeDecisionRuleIDReference getReference() {
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		return (o instanceof PolicySetIDReference)
				&& ((PolicySetIDReference)o).equalsTo(this);
	}

	/**
	 * A static helper method to detect cyclic references
	 *
	 * @param ref a policy set id reference
	 * @param context an evaluation context
	 * @return <code>true</code> if a given reference
	 */
	private static boolean isReferenceCyclic(PolicySetIDReference ref,
			EvaluationContext context)
	{
		if(context.getCurrentPolicySetIDReference() != null){
			if(ref.equals(context.getCurrentPolicySetIDReference())){
				throw new IllegalStateException("Cyclic reference detected");
			}
			return isReferenceCyclic(ref, context.getParentContext());
		}
		return false;
	}

	class PolicySetIDReferenceEvaluationContext extends DelegatingEvaluationContext
	{
		PolicySetIDReferenceEvaluationContext(
				EvaluationContext context) {
			super(context);
			Preconditions.checkArgument(!isReferenceCyclic(PolicySetIDReference.this, context));
		}

		@Override
		public PolicySetIDReference getCurrentPolicySetIDReference() {
			return PolicySetIDReference.this;
		}

		@Override
		public EvaluationContext getParentContext() {
			return getDelegate();
		}
	}

	public static class Builder extends BaseCompositeDecisionRuleIDReference.Builder<Builder>
	{
		@Override
		protected Builder getThis() {
			return this;
		}

		public PolicySetIDReference build(){
			return new PolicySetIDReference(this);
		}
	}
}
