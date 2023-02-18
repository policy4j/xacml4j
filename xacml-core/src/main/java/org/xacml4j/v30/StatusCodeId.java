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

import java.util.Map;
import java.util.Optional;

/**
 * XACML response status identifier
 *
 * @author Giedrius Trumpickas
 */
public class StatusCodeId extends SemanticalIdentifier
{
	public static final StatusCodeId OK = new StatusCodeId("urn:oasis:names:tc:xacml:1.0:status:ok", "ok");
	public static final StatusCodeId MISSING_ATTRIBUTE = new StatusCodeId("urn:oasis:names:tc:xacml:1.0:status:missing-attribute", "missing-attribute");
	public static final StatusCodeId SYNTAX_ERROR = new StatusCodeId("urn:oasis:names:tc:xacml:1.0:status:syntax-error", "syntax-error");
	public static final StatusCodeId STATUS_PROCESSING_ERROR = new StatusCodeId("urn:oasis:names:tc:xacml:1.0:status:processing-error", "processing-error");
	public static final StatusCodeId UNKNOWN = new StatusCodeId("urn:oasis:names:tc:xacml:1.0:status:unknown", "unknown");


	private final static Map<String, StatusCodeId> By_ID = SemanticalIdentifier.getById(StatusCodeId.class);
	private final static Map<String, StatusCodeId> By_ABBREVIATED_ID = SemanticalIdentifier.getByAbbrId(StatusCodeId.class);


	protected StatusCodeId(String id, String abbreviatedId) {
		super(id, abbreviatedId);
	}

	public static Optional<StatusCodeId> of(String id) {
		return of(id, By_ID, By_ABBREVIATED_ID,
				()->Optional.of(new StatusCodeId(id, id)), false);
	}
}
