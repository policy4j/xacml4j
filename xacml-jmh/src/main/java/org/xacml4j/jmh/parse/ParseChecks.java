package org.xacml4j.jmh.parse;

/*
 * #%L
 * Xacml4J JMH Benchmarks
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import java.util.List;

/** The setup check of the parse benchmarks: a broken parser cannot produce numbers. */
final class ParseChecks
{
	private ParseChecks(){
	}

	/**
	 * @param what "policy" or "request", for the message
	 * @param names the document names, in input order
	 * @param expected the models the corpus holds
	 * @param parsed the models the benchmark's parser produced, in the same order
	 * @throws IllegalStateException naming the first document whose model differs
	 */
	static void requireSame(String what, List<String> names, List<?> expected, List<?> parsed)
	{
		if(expected.size() != parsed.size() || names.size() != expected.size()){
			throw new IllegalStateException("Parsed " + parsed.size() + " " + what + " documents, expected " + expected.size());
		}
		for(int i = 0; i < expected.size(); i++){
			if(!expected.get(i).equals(parsed.get(i))){
				throw new IllegalStateException("The parsed " + what + " document " + names.get(i)
						+ " differs from the corpus model");
			}
		}
	}
}
