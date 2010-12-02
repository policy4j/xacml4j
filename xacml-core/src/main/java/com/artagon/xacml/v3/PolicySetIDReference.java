package com.artagon.xacml.v3;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * A XACML {@link PolicySet} reference
 * 
 * @author Giedrius Trumpickas
 */
public class PolicySetIDReference extends BaseCompositeDecisionRuleIDReference 
	implements PolicyElement
{

	public PolicySetIDReference(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		super(id, version, earliest, latest);
	}
	
	public PolicySetIDReference(String id, VersionMatch version) {
		super(id, version, null, null);
	}
	
	public PolicySetIDReference(String id, Version version) {
		super(id, VersionMatch.parse(version.getValue()), null, null);
	}

	/**
	 * Creates {@link PolicySetIDReference}
	 * 
	 * @param policyId a policy identifier
	 * @param version a policy version match
	 * @param earliest a policy earliest version match
	 * @param latest a policy latest version match
	 * @return {@link PolicySetIDReference} instance
	 * @throws XacmlSyntaxException if syntax error occurs
	 */
	public static PolicySetIDReference create(String policyId, String version, 
			String earliest, String latest) throws XacmlSyntaxException
	{
		return new PolicySetIDReference(policyId,
				(version != null)?VersionMatch.parse(version):null,
				(earliest != null)?VersionMatch.parse(earliest):null,
				(latest != null)?VersionMatch.parse(latest):null);
	}
	
	/**
	 * Tests if a given {@link PolicySet} is referenced
	 * by this reference
	 * 
	 * @param policySet a policy set
	 * @return <code>true</code> if this reference
	 * references given policy set
	 */
	public boolean isReferenceTo(PolicySet policySet) {
		return policySet != null && matches(policySet.getId(), policySet.getVersion());
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof PolicySetIDReference)){
			return false;
		}
		PolicySetIDReference r = (PolicySetIDReference)o;
		return r.getId().equals(getId()) 
		&& Objects.equal(getVersionMatch(), r.getVersionMatch()) 
		&& Objects.equal(getEarliestVersion(), r.getEarliestVersion()) 
		&& Objects.equal(getLatestVersion(), r.getLatestVersion());
	}
	
	/**
	 * Creates an {@link EvaluationContext} to evaluate this reference.
	 */
	@Override
	public EvaluationContext createContext(EvaluationContext context)
	{
		if(context.getCurrentPolicySetIDReference() ==  this){
			return context;
		}
		PolicySetIDReferenceEvaluationContext refContext = new PolicySetIDReferenceEvaluationContext(context);
		try{
			PolicySet policySet = refContext.resolve(this);
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
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return Decision.INDETERMINATE;
		}
		PolicySet ps = context.getCurrentPolicySet();
		Preconditions.checkState(ps != null);
		return ps.evaluate(context);
	}

	@Override
	public Decision evaluateIfApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return Decision.INDETERMINATE;
		}
		PolicySet ps = context.getCurrentPolicySet();
		Preconditions.checkState(ps != null);
		return ps.evaluateIfApplicable(context);
	}

	@Override
	public MatchResult isApplicable(EvaluationContext context) {
		Preconditions.checkArgument(context.getCurrentPolicySetIDReference() == this);
		if(!isReferenceTo(context.getCurrentPolicySet())){
			return MatchResult.INDETERMINATE;
		}
		PolicySet ps = context.getCurrentPolicySet();
		Preconditions.checkState(ps != null);
		return ps.isApplicable(context);
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
	}
}
