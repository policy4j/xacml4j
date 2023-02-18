package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2022 Xacml4J.org
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.net.MediaType;

public final class PolicyImportTool {
	private Map<MediaType, Unmarshaller<CompositeDecisionRule>> unmarshallers;
	private PolicyRepository repository;

	PolicyImportTool(
			PolicyRepository repository,
			Unmarshaller<CompositeDecisionRule> ...unmarshal) {
		this.unmarshallers = Arrays.asList(Objects.requireNonNull(unmarshal, "unmarshal"))
		                           .stream().collect(Collectors.toMap(v->v.getMediaType(), v->v));
		this.repository = Objects.requireNonNull(repository, "repository");

	}

	/**
	 * Imports a given XACML policy from a given
	 * {@link InputStream} supplier. Policy repository will close
	 * input stream after importing the policy.
	 *
	 * @param mediaType a media type
	 * @param policySource a policy source
	 * @return {@link CompositeDecisionRule} an imported
	 * {@link Policy} or {@link PolicySet}
	 * @throws SyntaxException if an syntax error
	 *                         occurs while parsing XACML policy or policy set
	 * @throws IOException     if an IO error occurs
	 */
	public CompositeDecisionRule importPolicy(MediaType mediaType,
	                                          Supplier<InputStream> policySource)
			throws SyntaxException, IOException {
		InputStream is = null;
		try {
			is = policySource.get();
			Unmarshaller<CompositeDecisionRule> u = unmarshallers.get(mediaType);
			if(u != null){
				CompositeDecisionRule r = u.unmarshal(policySource.get());
				repository.add(r);
			}
			return null;
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	public void importPolicy(MediaType mediaType, Path path)
			throws SyntaxException, IOException {
		Iterator<Path> it = path.normalize().iterator();
		if (it.hasNext()) {
			BufferedReader reader = Files.newBufferedReader(path);
			byte[] bytes = CharStreams.toString(reader).getBytes(Charsets.UTF_8);
			importPolicy(mediaType, () -> new ByteArrayInputStream(bytes));
		}
		while (it.hasNext()) {
			importPolicy(mediaType, it.next());
		}
	}
}
