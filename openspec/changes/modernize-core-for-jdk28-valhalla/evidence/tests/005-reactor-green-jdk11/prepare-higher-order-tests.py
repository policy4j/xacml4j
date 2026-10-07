from pathlib import Path
import hashlib
import json
import re

repo = Path.cwd()
ev = Path(__file__).parent
source = "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java"
protected = json.loads((ev / "39-protected-before-identifiers.json").read_text())
assert all((repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest
           for name, digest in protected.items())
protected.pop(source)
for name in json.loads((ev / "39-identifier-sources-before.json").read_text()):
    protected[name] = hashlib.sha256((repo / name).read_bytes()).hexdigest()
duration_test = "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DurationFunctionIdentifierTest.java"
protected[duration_test] = hashlib.sha256((repo / duration_test).read_bytes()).hexdigest()
(ev / "43-protected-before-higher-order-identifiers.json").write_text(json.dumps(protected, indent=2) + "\n")
(ev / "43-higher-order-source-before.json").write_text(json.dumps({source: (repo / source).read_text()}, indent=2) + "\n")
test = repo / "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionIdentifierTest.java"
assert not test.exists()
header = re.search(r"/\*.*?#L%.*?\*/", (repo / duration_test).read_text(), re.DOTALL).group()
body = '''
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;

/**
 * XACML 3.0 Appendix A.3.12 specifies four 3.0 names and three unchanged
 * 1.0 names; A.4 and Table 10.2.9 retain the four older 1.0 names:
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 */
@RunWith(Parameterized.class)
public class HigherOrderFunctionIdentifierTest
{
\tprivate static final FunctionProvider FUNCTIONS =
\t\t\tFunctionProviderBuilder.builder().withDefaultFunctions().build();

\tprivate final String identifier;
\tprivate final String canonical;
\tprivate final String legacy;

\tpublic HigherOrderFunctionIdentifierTest(String identifier, String canonical, String legacy)
\t{
\t\tthis.identifier = identifier;
\t\tthis.canonical = canonical;
\t\tthis.legacy = legacy;
\t}

\t@Parameterized.Parameters(name="{0}")
\tpublic static Collection<Object[]> identifiers()
\t{
\t\tList<Object[]> identifiers = new ArrayList<>();
\t\tfor (String operation : new String[]{"any-of", "all-of", "any-of-any", "map"}) {
\t\t\tString canonical = "urn:oasis:names:tc:xacml:3.0:function:" + operation;
\t\t\tString legacy = "urn:oasis:names:tc:xacml:1.0:function:" + operation;
\t\t\tidentifiers.add(new Object[]{canonical, canonical, legacy});
\t\t\tidentifiers.add(new Object[]{legacy, canonical, legacy});
\t\t}
\t\tfor (String operation : new String[]{"all-of-any", "any-of-all", "all-of-all"}) {
\t\t\tString identifier = "urn:oasis:names:tc:xacml:1.0:function:" + operation;
\t\t\tidentifiers.add(new Object[]{identifier, identifier, null});
\t\t}
\t\treturn identifiers;
\t}

\t@Test
\tpublic void resolvesEveryStandardHigherOrderName()
\t{
\t\tOptional<FunctionSpec> resolved = FUNCTIONS.getFunction(identifier);
\t\tassertTrue("Missing required function " + identifier, resolved.isPresent());
\t\tassertEquals(canonical, resolved.get().getId());
\t\tif (legacy != null) {
\t\t\tassertTrue("Missing legacy alias " + legacy, resolved.get().getLegacyId().isPresent());
\t\t\tassertEquals(legacy, resolved.get().getLegacyId().get());
\t\t\tassertSame(resolved.get(), FUNCTIONS.getFunction(canonical).get());
\t\t\tassertSame(resolved.get(), FUNCTIONS.getFunction(legacy).get());
\t\t}
\t}
}
'''
test.write_text("package org.xacml4j.v30.policy.function.impl;\n\n" + header + "\n" + body)
print(f"Protected files: {len(protected)}; 11 lookup tests written before production edits")
