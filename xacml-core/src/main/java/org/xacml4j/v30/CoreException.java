package org.xacml4j.v30;

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



/**
 * Root exception for all XACML related issues :)
 *
 * @author Giedrius Trumpickas
 */
public class CoreException extends RuntimeException
{
	private static final long serialVersionUID = -546790992581476011L;

	// Read once, when the class loads: set it when the JVM starts (design D20)
	private static final boolean STACK_TRACE = Boolean.getBoolean("xacml4j.exceptions.stackTrace");

	private Status status;

	/**
	 * Constructs exception with a given status and {@link Throwable}
	 *
	 * @param status a XACML status
	 */
	protected CoreException(Status status,
	                        String message){
		super(message);
		this.status = Status
				.from(status)
				.message(message)
				.build();
	}

	protected CoreException(Status status,
	                        Throwable t){
		super(status.getMessage().orElse(null), t);
		this.status = Status.from(status)
		                    .error(t)
		                    .build();
	}

	protected CoreException(Status status,
							String message,
	                        Throwable t){
		super(message, t);
		this.status = Status.from(status)
		                    .error(t)
		                    .build();
	}

	/**
	 * Engine exceptions fill no stack trace unless the JVM starts with
	 * {@code -Dxacml4j.exceptions.stackTrace=true} (design D20)
	 */
	@Override
	public Throwable fillInStackTrace(){
		return STACK_TRACE ? super.fillInStackTrace() : this;
	}

	/**
	 * Gets XACML {@link EvaluationContext}
	 *
	 * @return {@link EvaluationContext}
	 */
	public final Status getStatus(){
		return status;
	}

	public static String format(String template, Object ...p){
		return (template == null)?null:String.format(template, p);
	}
}

