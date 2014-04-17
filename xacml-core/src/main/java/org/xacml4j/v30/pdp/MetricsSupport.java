package org.xacml4j.v30.pdp;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public final class MetricsSupport 
{
	private  final static String XACML4J_NAME_REFIX = "org.xacml4j";
	private final static String PDP_METRICS_REGISTRY_NAME = "org.xacml4j.pdp.metricsRegistry";
	
	
	public static MetricRegistry getOrCreate(){
		return SharedMetricRegistries.getOrCreate(PDP_METRICS_REGISTRY_NAME);
	}
	
	public static String name(String ...n){
		return MetricRegistry.name(XACML4J_NAME_REFIX, n);
	}
}
