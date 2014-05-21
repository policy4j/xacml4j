package org.xacml4j.v30.pdp;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public final class MetricsSupport
{
	private final static String XACML4J_NAME_PREFIX = "org.xacml4j";
	private final static String PDP_METRICS_REGISTRY_NAME = "org.xacml4j.pdp.metricsRegistry";

	/** Private constructor for utility class */
	private MetricsSupport() {}

	public static MetricRegistry getOrCreate(){
		return SharedMetricRegistries.getOrCreate(PDP_METRICS_REGISTRY_NAME);
	}

	public static String name(String ...n){
		return MetricRegistry.name(XACML4J_NAME_PREFIX, n);
	}
}
