from pathlib import Path
import difflib

repo = Path.cwd()
ev = Path(__file__).parent
name = "xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java"
before = (repo / name).read_text()
candidate = before
for kind in ("dayTimeDuration", "yearMonthDuration"):
    for suffix in ("one-and-only", "bag-size", "is-in", "bag"):
        legacy = "urn:oasis:names:tc:xacml:1.0:function:" + kind + "-" + suffix
        current = legacy.replace(":1.0:", ":3.0:", 1)
        old = '\t@XacmlFuncSpec(id="' + legacy + '")'
        new = '\t@XacmlFuncSpec(id="' + current + '")\n\t@XacmlLegacyFunc(id="' + legacy + '")'
        assert candidate.count(old) == 1
        candidate = candidate.replace(old, new, 1)
patch = "diff --git a/" + name + " b/" + name + "\n" + "".join(difflib.unified_diff(
    before.splitlines(keepends=True), candidate.splitlines(keepends=True),
    fromfile="a/" + name, tofile="b/" + name))
(ev / "required-duration-bag-alias-fix.patch").write_text(patch)
assert (repo / name).read_text() == before
print("Unapplied BagFunctions proposal: eight standard 3.0 identifiers with existing 1.0 aliases retained")
