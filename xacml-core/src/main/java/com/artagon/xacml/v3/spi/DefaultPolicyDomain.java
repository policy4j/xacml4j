package com.artagon.xacml.v3.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.CompositeDecisionRuleIDReference;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.EvaluationContext;
import com.google.common.base.Preconditions;

/**
 * A default implementation of {@link PolicyDomain}
 * 
 * @author Giedrius Trumpickas
 */
public final class DefaultPolicyDomain 
	implements PolicyDomain
{
	private final static Logger log = LoggerFactory.getLogger(DefaultPolicyDomain.class);
	
	private final static Map<Type, String> MODE = new HashMap<PolicyDomain.Type, String>();
	
	static
	{
		MODE.put(Type.DENY_OVERRIDES, "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides");
		MODE.put(Type.FIRST_APPLICABLE, "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable");
		MODE.put(Type.ONLY_ONE_APPLICABLE, "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable");
	}
	
	private String name;
	private Type mode;
	
	private DecisionCombiningAlgorithm<CompositeDecisionRule> combineDecision;
	private Map<String, CompositeDecisionRule> policies;
	
	
	public DefaultPolicyDomain(String name, 
			Type mode, 
			DecisionCombiningAlgorithmProvider decisionAlgorithmProvider,
			CompositeDecisionRule ...domainPolicies){
		this(name, mode, decisionAlgorithmProvider, 
				Arrays.asList(domainPolicies));
	}
	public DefaultPolicyDomain(String name, 
			Type mode, 
			DecisionCombiningAlgorithmProvider decisionAlgorithmProvider,
			Collection<CompositeDecisionRule> domainPolicies)
	{
		Preconditions.checkArgument(name != null);
		Preconditions.checkArgument(mode != null);
		Preconditions.checkArgument(decisionAlgorithmProvider != null);
		this.name = name;
		this.mode = mode;
		String algorithmId = MODE.get(mode);
		Preconditions.checkState(algorithmId != null);
		this.combineDecision = decisionAlgorithmProvider.getPolicyAlgorithm(algorithmId);
		Preconditions.checkState(combineDecision != null);
		this.policies = new ConcurrentHashMap<String, CompositeDecisionRule>();
		for(CompositeDecisionRule r : domainPolicies){
			add(r);
		}
	}
	

	@Override
	public String getName() {
		return name;
	}

	private void add(CompositeDecisionRule policy) {
		CompositeDecisionRuleIDReference r = policy.getReference();
		if(log.isDebugEnabled()){
			log.debug("Adding composite rule reference id=\"{}\" version=\"{}\", " +
					"EarliestVersion=\"{}\", LatestVersion=\"{}\" to the policy domain=\"{}\"", 
					new String[]{r.getId(), 
					r.getVersionMatch() == null?"any":r.getVersionMatch().getPattern(),
					r.getEarliestVersion() == null?"any":r.getEarliestVersion().getPattern(),
					r.getLatestVersion() == null?"any":r.getLatestVersion().getPattern(), 
					name
				});
		}
		CompositeDecisionRule oldPolicy = policies.put(r.getId(), r);
		Preconditions.checkState(oldPolicy == null);
	}
		
	@Override
	public Collection<CompositeDecisionRule> getDomainPolicies() {
		return Collections.unmodifiableCollection(policies.values());
	}

	@Override
	public Type getMode(){
		return mode;
	}
	
	@Override
	public final Decision evaluate(EvaluationContext context) {
		List<CompositeDecisionRule> policies = new LinkedList<CompositeDecisionRule>(this.policies.values());
		return combineDecision.combine(policies, context);
	}
}
