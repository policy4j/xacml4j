from pathlib import Path
import struct
import sys


modules = [
    "xacml-core",
    "xacml-xml-jaxb",
    "xacml-xml",
    "xacml-json",
    "xacml-pdp",
    "xacml-test",
    "xacml-conformance-tests",
]
failed = False

for module in modules:
    for output in ("classes", "test-classes"):
        root = Path(module) / "target" / output
        files = sorted(
            path
            for path in root.rglob("*.class")
            if path.name not in {"module-info.class", "package-info.class"}
        ) if root.exists() else []
        if not files:
            print(f"{module} {output}: no class files")
            continue
        sample = files[0]
        magic, minor, major = struct.unpack(">IHH", sample.read_bytes()[:8])
        status = "OK" if magic == 0xCAFEBABE and major == 65 else "FAIL"
        print(f"{module} {output}: {sample.as_posix()} major={major} {status}")
        failed |= status != "OK"

sys.exit(1 if failed else 0)
