package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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

import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;

import java.util.Optional;


@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	private FunctionSpec spec;

	public FunctionInvocationException(
			FunctionSpec spec,
			String message,
			Object ...params) {
		this(spec,  null, message, params);
	}

	public FunctionInvocationException(
			FunctionSpec spec,
			Throwable cause) {
		this(spec, cause, null);
	}

	public FunctionInvocationException(
			FunctionSpec spec,
			Throwable cause,
			String message, Object...params) {
		super(Status.processingError()
				.message(Optional.ofNullable(message)
				                 .map(v-> String.format(v, params)).orElse(null))
				.detail(cause).build(), cause);
		this.spec = spec;
	}



	public FunctionSpec getSpec(){
		return spec;
	}
}
