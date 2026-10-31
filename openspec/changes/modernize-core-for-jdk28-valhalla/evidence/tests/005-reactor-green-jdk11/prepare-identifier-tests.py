from pathlib import Path
import hashlib
import json
import re

repo = Path.cwd()
ev = Path(__file__).parent
previous = json.loads((ev / "34-protected-before-date-arithmetic.json").read_text())
assert all((repo / name).is_file() and hashlib.sha256((repo / name).read_bytes()).hexdigest() == digest
           for name, digest in previous.items())
sources = ["xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/" + name
           for name in ("BagFunctions.java", "SetFunctions.java")]
new_test = "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DurationFunctionIdentifierTest.java"
assert not (repo / new_test).exists()
protected = {name: digest for name, digest in previous.items() if name not in sources}
for pattern in ("xacml-core/src/**/*.java", "xacml-pdp/src/**/*.java", "xacml-test/src/**/*.java",
                "xacml-xml/src/**/*.java", "xacml-conformance-tests/src/test/java/**/*.java"):
    for path in repo.glob(pattern):
        name = str(path.relative_to(repo))
        if name not in sources:
            protected[name] = hashlib.sha256(path.read_bytes()).hexdigest()
protected["xacml-test/pom.xml"] = hashlib.sha256((repo / "xacml-test/pom.xml").read_bytes()).hexdigest()
(ev / "39-protected-before-identifiers.json").write_text(json.dumps(protected, indent=2) + "\n")
(ev / "39-identifier-sources-before.json").write_text(json.dumps(
    {name: (repo / name).read_text() for name in sources}, indent=2) + "\n")
for source, archive in (("summary.md", "36-summary-before-identifiers.md"),
                        ("conformance-blockers.md", "36-blockers-before-identifiers.md"),
                        ("scope-check.txt", "36-scope-before-identifiers.txt")):
    assert not (ev / archive).exists()
    (ev / archive).write_bytes((ev / source).read_bytes())
template = (repo / "xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/BagFunctionsTest.java").read_text()
header = re.search(r"/\*.*?#L%.*?\*/", template, flags=re.DOTALL).group()
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
 * XACML 3.0 Appendix A.3.10 and A.3.11 define duration bag and set functions.
 * Tables 10.2.8 and 10.2.9 list their 3.0 identifiers and retained 1.0 names:
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 */
@RunWith(Parameterized.class)
public class DurationFunctionIdentifierTest
{
\tprivate static final FunctionProvider FUNCTIONS =
\t\t\tFunctionProviderBuilder.builder().withDefaultFunctions().build();

\tprivate final String identifier;
\tprivate final String canonical;
\tprivate final String legacy;

\tpublic DurationFunctionIdentifierTest(String identifier, String canonical, String legacy)
\t{
\t\tthis.identifier = identifier;
\t\tthis.canonical = canonical;
\t\tthis.legacy = legacy;
\t}

\t@Parameterized.Parameters(name="{0}")
\tpublic static Collection<Object[]> identifiers()
\t{
\t\tList<Object[]> identifiers = new ArrayList<>();
\t\tfor (String type : new String[]{"dayTimeDuration", "yearMonthDuration"}) {
\t\t\tfor (String operation : new String[]{"one-and-only", "bag-size", "is-in", "bag",
\t\t\t\t\t"intersection", "union", "subset", "at-least-one-member-of", "set-equals"}) {
\t\t\t\tString canonical = "urn:oasis:names:tc:xacml:3.0:function:" + type + "-" + operation;
\t\t\t\tString legacy = "urn:oasis:names:tc:xacml:1.0:function:" + type + "-" + operation;
\t\t\t\tidentifiers.add(new Object[]{canonical, canonical, legacy});
\t\t\t\tidentifiers.add(new Object[]{legacy, canonical, legacy});
\t\t\t}
\t\t}
\t\treturn identifiers;
\t}

\t@Test
\tpublic void resolvesStandardAndLegacyNamesToSameFunction()
\t{
\t\tOptional<FunctionSpec> resolved = FUNCTIONS.getFunction(identifier);
\t\tassertTrue("Missing required function " + identifier, resolved.isPresent());
\t\tassertEquals(canonical, resolved.get().getId());
\t\tassertTrue("Missing legacy alias " + legacy, resolved.get().getLegacyId().isPresent());
\t\tassertEquals(legacy, resolved.get().getLegacyId().get());
\t\tassertSame(resolved.get(), FUNCTIONS.getFunction(canonical).get());
\t\tassertSame(resolved.get(), FUNCTIONS.getFunction(legacy).get());
\t}
}
'''
(repo / new_test).write_text("package org.xacml4j.v30.policy.function.impl;\n\n" + header + "\n" + body)
print(f"Protected files: {len(protected)}; new resolution tests: 36; production unchanged")
