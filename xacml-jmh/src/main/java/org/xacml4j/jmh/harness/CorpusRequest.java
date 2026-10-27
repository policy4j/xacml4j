package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import java.util.Objects;

import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;

/**
 * A parsed request of a corpus and its expected decision.
 *
 * @param id the request id, unique within its corpus
 * @param request the parsed request
 * @param expected the expected decision; {@code INDETERMINATE} matches every Indeterminate value
 */
public record CorpusRequest(String id, RequestContext request, Decision expected)
{
	public CorpusRequest {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(expected, "expected");
	}

	/**
	 * @param actual a decision of the PDP
	 * @return whether it is the expected decision; an expected Indeterminate
	 * matches every Indeterminate value (XACML 3.0 §5.53, §7.10)
	 */
	public boolean matches(Decision actual){
		return expected.isIndeterminate() ? actual.isIndeterminate() : expected == actual;
	}
}
