package org.xacml4j.v30.spi.pdp;

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

import java.util.concurrent.atomic.AtomicLong;

import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;


public class NoCachePolicyDecisionCache implements PolicyDecisionCache
{
	private AtomicLong cacheMiss = new AtomicLong(0);

	@Override
	public long getCacheHitCount() {
		return 0;
	}

	@Override
	public long getCacheMissCount() {
		return cacheMiss.get();
	}

	@Override
	public void resetCount() {
		cacheMiss.set(0);
	}

	@Override
	public Result getDecision(RequestContext req) {
		cacheMiss.incrementAndGet();
		return null;
	}

	@Override
	public void putDecision(RequestContext req, Result res, int ttl) {
	}
}
