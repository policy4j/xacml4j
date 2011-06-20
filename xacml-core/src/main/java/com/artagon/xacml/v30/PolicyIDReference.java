package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public final class PolicyIDReference extends 
	BaseCompositeDecisionRuleIDReference implements PolicyElement
{
	public PolicyIDReference(
			String id, 
			VersionMatch version,
			VersionMatch earliest, 
			VersionMatch latest) {
		super(id, version, earliest, latest);
	}
	
	public PolicyIDReference(String id, VersionMatch version) {
		super(id, version, null, null);
	}
	
	public PolicyIDReference(String id, Version version) {
		super(id, VersionMatch.parse(version.getValue()), null, null);
	}
	
	/**
	 * Test this reference points to a given policy
	 * 
	 * @param policy a policy
	 * @return <code>true</code> if a this reference
	 * points to a given policys
	 */
	public boolean isReferenceTo(Policy policy) {
		return policy != null && 
		matches(policy.getId(), policy.getVersion());
	}
	
	/**
	 * Creates a {@link PolicyIDReference} instance
	 * 
	 * @param policyId a policy identifier
	 * @param version a policy version match
	 * @param earliest a policy earliest version match
	 * @param latest a policy latest version match
	 * @return {@link PolicyIDReference} instance
	 * @throws XacmlSyntaxException
	 */
	public static PolicyIDReference create(String policyId, String version, 
			String earliest, String latest) throws XacmlSyntaxException
	{
		return new PolicyIDReference(policyId,
				(version != null)?VersionMatch.parse(version):null,
				(earliest != null)?VersionMatch.parse(earliest):null,
				(latest != null)?VersionMatch.parse(latest):null);
	}
	
	@Override
	public CompositeDecisionRuleIDReference getReference() {
		return this;
	}

	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		if(context.getCurrentPolicyIDReference() ==  this){
			return context;
		}
		PolicyIDReferenceEvaluationContext refContext = new PolicyIDReferenceEvaluationContext(context);
		try{
			Policy resolvedPolicy = context.resolve(this);
			if(resolvedPolicy == null){
				return refContext;
			}
			return resolvedPolicy.createContext(refContext);
		}catch(PolicyResolutionException e){
			return refContext;
		}
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof PolicyIDReference)){
			return false;
		}
		PolicyIDReference r = (PolicyIDReference)o;
		return getId().equals(r.getId()) 
		&& Objects.equal(getVersion(), r.getVersion()) 
		&& Objects.equal(getEarliestVersion(), r.getEarliestVersion()) 
		&& Objects.equal(getLatestVersion(), r.getLatestVersion());
	}

	@Override
	public Decision evaluate(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return Decision.INDETERMINATE;
		}
		Policy p = context.getCurrentPolicy();
		Preconditions.checkState(p != null);
		return p.evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return Decision.INDETERMINATE;
		}
		Policy p = context.getCurrentPolicy();
		Preconditions.checkState(p != null);
		return p.evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicyIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicy())){
			return MatchResult.INDETERMINATE;
		}
		Policy p = context.getCurrentPolicy();
		Preconditions.checkState(p != null);
		return p.isApplicable(context);
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}	
	
	/**
	 * Validates if a given reference is cyclic 
	 * in a given evaluation context
	 * 
	 * @param ref a reference
	 * @param context an evaluation context
	 * @return
	 */
	private static boolean isReferenceCyclic(PolicyIDReference ref, 
			EvaluationContext context)
	{
		if(context.getCurrentPolicyIDReference() != null){
			if(ref.equals(context.getCurrentPolicyIDReference())){
				throw new IllegalStateException(
						String.format(
								"Reference=\"%s\" is cyclic", ref      ));
			}
			return isReferenceCyclic(ref, context.getParentContext());
		}
		return false;				
	}
	
	/**
	 * An {@link EvaluationContext} implementation
	 * to evaluate {@link PolicySetIDReference} decisions
	 * 
	 * @author Giedrius Trumpickas
	 */
	class PolicyIDReferenceEvaluationContext extends
			DelegatingEvaluationContext 
	{
		/**
		 * Creates policy evaluation context with a given parent context
		 * 
		 * @param context a parent evaluation context
		 * @param policyIDRef a policy reference
		 * @exception IllegalArgumentException if enclosing context
		 * {@link EvaluationContext#getCurrentPolicySet()} returns
		 * <code>null</code> or given policy ID reference is
		 * <code>null</code>
		 */
		PolicyIDReferenceEvaluationContext(EvaluationContext context){
			super(context);
			Preconditions.checkArgument(context.getCurrentPolicy() == null);
			Preconditions.checkArgument(!isReferenceCyclic(PolicyIDReference.this, context));
		}
		
		@Override
		public PolicyIDReference getCurrentPolicyIDReference() {
			return PolicyIDReference.this;
		}
	}

}
