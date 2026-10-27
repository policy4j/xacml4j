package org.xacml4j.jmh.testing;

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

import java.util.Optional;

import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;

/** The other corpus named {@code twin}; see {@link FirstTwinCorpus}. */
public final class SecondTwinCorpus implements PolicyCorpusProvider
{
	@Override
	public String name(){
		return FirstTwinCorpus.NAME;
	}

	@Override
	public PolicyCorpus load(Optional<String> argument){
		throw new UnsupportedOperationException("A twin corpus is never selected");
	}
}
