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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class VendoredFixtureIntegrityTest
{
	@Test
	public void vendoredStandardsMatchRecordedGitBlobs() throws IOException
	{
		Path repositoryRoot = GenerateVendoredFixtureManifest.findRepositoryRoot(Paths.get("").toAbsolutePath());
		String override = System.getProperty(GenerateVendoredFixtureManifest.FIXTURE_ROOT_PROPERTY);
		Path fixtureRoot = override == null
				? repositoryRoot
				: Paths.get(override).toAbsolutePath().normalize();
		Path manifest = repositoryRoot.resolve(GenerateVendoredFixtureManifest.MANIFEST_PATH);

		Map<String, String> expected = GenerateVendoredFixtureManifest.readManifest(manifest);
		Map<String, String> actual = GenerateVendoredFixtureManifest.collectBlobs(fixtureRoot);
		List<String> differences = GenerateVendoredFixtureManifest.differences(expected, actual);
		assertTrue(differences.isEmpty(), () -> "Vendored fixture integrity check failed:\n"
				+ String.join("\n", differences));
	}
}
