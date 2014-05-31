package org.xacml4j.v30.pdp;

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


public class XacmlException extends RuntimeException
{
	private static final long serialVersionUID = -546790992581476011L;

	protected XacmlException(Throwable cause){
		super(cause);
	}

	protected XacmlException(String message){
		super(message);
	}

	/**
	 * Constructs exception with a given message.
	 *
	 * @param template a template {@link String#format(String, Object...)}
	 * @param arguments an arguments for template
	 */
	protected XacmlException(
			String template, Object ... arguments){
		super(String.format(template, arguments));
	}

	/**
	 * Constructs exception with a given status and message.
	 *
	 * @param cause a root cause of this exception
	 * @param message a template {@link String#format(String, Object...)}
	 * @param arguments an arguments for template
	 */
	protected XacmlException(Throwable cause,
			String message, Object ... arguments){
		super(String.format(message, arguments), cause);
	}


}

