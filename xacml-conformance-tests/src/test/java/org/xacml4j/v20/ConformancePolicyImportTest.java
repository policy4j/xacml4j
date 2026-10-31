package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Conformance Tests
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.spi.repository.PolicyImportTool;

public class ConformancePolicyImportTest
{
	@Test
	public void missingPolicyFailsWithCaseAndResourceName()
	{
		PolicyImportTool tool = ConformanceCaseRunner.newRepository("missing-policy").newImportTool();
		AssertionError failure = assertThrows(AssertionError.class,
				() -> ConformanceCaseRunner.importPolicy(tool, MediaType.Type.XACML20_XML,
						"oasis20/IIA999", "missing-policy.xml", () -> null));
		assertTrue(failure.getMessage().contains("oasis20/IIA999"));
		assertTrue(failure.getMessage().contains("missing-policy.xml"));
		assertNotNull(failure.getCause());
	}

	@Test
	public void malformedPolicyFailsWithOriginalMappingError()
	{
		PolicyImportTool tool = ConformanceCaseRunner.newRepository("malformed-policy").newImportTool();
		AssertionError failure = assertThrows(AssertionError.class,
				() -> ConformanceCaseRunner.importPolicy(tool, MediaType.Type.XACML30_XML,
						"oasis30/IIA999", "malformed-policy.xml",
						() -> new ByteArrayInputStream("<Policy>".getBytes(StandardCharsets.UTF_8))));
		assertTrue(failure.getMessage().contains("oasis30/IIA999"));
		assertTrue(failure.getMessage().contains("malformed-policy.xml"));
		assertNotNull(failure.getCause());
	}

	@Test
	public void importsXacml30PoliciesWithXacml30MediaType()
	{
		assertEquals(MediaType.Type.XACML30_XML, ConformanceCaseRunner.mediaType("oasis30"));
		assertEquals(MediaType.Type.XACML20_XML, ConformanceCaseRunner.mediaType("oasis20"));
	}
}

