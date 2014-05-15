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

import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;


@SuppressWarnings("serial")
public class FunctionInvocationException extends EvaluationException
{
	private FunctionSpec spec;

	public FunctionInvocationException(
			FunctionSpec spec,
			String template, Object... arguments) {
		super(Status.processingError().build(), template, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(
			FunctionSpec spec,
			Throwable cause, String message,
			Object... arguments) {
		super(Status.processingError().build(),
				cause, message, arguments);
		this.spec = spec;
	}

	public FunctionInvocationException(FunctionSpec spec, Throwable cause) {
		super(Status.processingError().build(), cause);
		this.spec = spec;
	}

	public FunctionSpec getSpec(){
		return spec;
	}
}
