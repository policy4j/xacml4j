package com.artagon.xacml.v30;

import org.apache.log4j.MDC;

import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.artagon.xacml.v30.spi.pip.ContentResolver;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;

public class MDCSupport 
{
	private final static String PDP_ID_KEY = "pdpId";
	private final static String PIP_ID_KEY = "pipId";
	private final static String POLICY_ID_KEY = "policyId";
	private final static String POLICYSET_ID_KEY = "policySetId";
	private final static String RULE_ID_KEY = "ruleId";
	private final static String ATTR_RESOLVER_ID_KEY = "attributeResolverId";
	private final static String CONT_RESOLVER_ID_KEY = "contentResolverId";
	
	public static void setPdpContext(PolicyDecisionPoint pdp){
		MDC.put(PDP_ID_KEY, pdp.getId());
	}
	
	public static void cleanPdpContext(){
		MDC.remove(PDP_ID_KEY);
	}
	
	public static void setPipContext(PolicyInformationPoint pip){
		MDC.put(PIP_ID_KEY, pip.getId());
	}
	
	public static void cleanPipContext(){
		MDC.remove(PIP_ID_KEY);
	}
	
	public static void setPolicyContext(Policy p){
		MDC.put(POLICY_ID_KEY, p.getId());
	}
	
	public  static void cleanPolicyContext(){
		MDC.remove(POLICY_ID_KEY);
	}
	
	public static void setPolicySetContext(PolicySet p){
		MDC.put(POLICYSET_ID_KEY, p.getId());
	}
	
	public static void cleanPolicySetContext(){
		MDC.remove(POLICYSET_ID_KEY);
	}
	
	public static void setRuleContext(Rule p){
		MDC.put(RULE_ID_KEY, p.getId());
	}
	
	public static void cleanRuleContext(){
		MDC.remove(RULE_ID_KEY);
	}
	
	public static void setAttributeResolverContext(AttributeResolver r){
		MDC.put(ATTR_RESOLVER_ID_KEY, r.getDescriptor().getId());
	}
	
	public static void cleanAttributeResolverContext(){
		MDC.remove(ATTR_RESOLVER_ID_KEY);
	}
	
	public static void setContentResolverContext(ContentResolver r){
		MDC.put(CONT_RESOLVER_ID_KEY, r.getDescriptor().getId());
	}
	
	public static void cleanContentResolverContext(){
		MDC.remove(CONT_RESOLVER_ID_KEY);
	}
}
