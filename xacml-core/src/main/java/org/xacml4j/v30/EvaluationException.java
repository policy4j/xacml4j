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

import java.util.Optional;


/**
 * XACML {@link Expression} evaluation exception,
 * indicating runtime expression evaluation failure
 *
 * @author Giedrius Trumpickas
 */
public class EvaluationException extends CoreException
{
	private Optional<EvaluationContext> context;

	public EvaluationException(EvaluationContext context,
							   Throwable cause) {
		super(Status
				.from(Optional
						.ofNullable(context)
						.flatMap(v->v.getEvaluationStatus())
								.orElse(Status
										.processingError()
										.build()))
				.build(), cause);
		this.context = Optional.ofNullable(context);
	}


	public EvaluationException(
			Status status,
			Throwable cause) {
		super(status, cause);
	}

	public EvaluationException(
			Status status,
			String m, Object ...p) {
		super(status, String.format(m, p));
	}

	public Optional<EvaluationContext> getContext(){
		return context;
	}

}
