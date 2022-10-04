package org.xacml4j.v30.spi.repository;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.Unmarshaller;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

public final class PolicyImportTool {
	private Unmarshaller<CompositeDecisionRule> unmarshal;
	private PolicyRepository repository;

	PolicyImportTool(
			Unmarshaller<CompositeDecisionRule> unmarshal,
			PolicyRepository repository) {
		this.unmarshal = Objects.requireNonNull(unmarshal, "unmarshal");
		this.repository = Objects.requireNonNull(repository, "repository");

	}

	/**
	 * Imports a given XACML policy from a given
	 * {@link InputStream} supplier. Policy repository will close
	 * input stream after importing the policy.
	 *
	 * @param source a policy source
	 * @return {@link CompositeDecisionRule} an imported
	 * {@link Policy} or {@link PolicySet}
	 * @throws SyntaxException if an syntax error
	 *                         occurs while parsing XACML policy or policy set
	 * @throws IOException     if an IO error occurs
	 */
	public CompositeDecisionRule importPolicy(Supplier<InputStream> policySource)
			throws SyntaxException, IOException {
		InputStream is = null;
		try {
			is = policySource.get();
			CompositeDecisionRule r = unmarshal.unmarshal(is);
			repository.add(r);
			return r;
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	public void importPolicy(Path path)
			throws SyntaxException, IOException {
		Iterator<Path> it = path.normalize().iterator();
		if (it.hasNext()) {
			BufferedReader reader = Files.newBufferedReader(path);
			byte[] bytes = CharStreams.toString(reader).getBytes(Charsets.UTF_8);
			importPolicy(() -> new ByteArrayInputStream(bytes));
		}
		while (it.hasNext()) {
			importPolicy(it.next());
		}
	}
}
