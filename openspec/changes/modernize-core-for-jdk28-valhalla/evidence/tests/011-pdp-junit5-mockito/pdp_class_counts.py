from pathlib import Path
import sys
import xml.etree.ElementTree as ET


label = sys.argv[1]
if label not in {"before", "after"}:
    raise SystemExit("usage: pdp_class_counts.py before|after")

reports = sorted(Path("xacml-pdp/target/surefire-reports").glob("TEST-*.xml"))
rows = []
for report in reports:
    root = ET.parse(report).getroot()
    rows.append(
        (
            root.get("name", report.stem.removeprefix("TEST-")),
            *(int(root.get(key, "0")) for key in ("tests", "failures", "errors", "skipped")),
        )
    )

output = Path(
    "openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/"
    f"011-pdp-junit5-mockito/pdp-class-counts-{label}.tsv"
)
output.write_text(
    "class\ttests\tfailures\terrors\tskipped\n"
    + "".join("\t".join(map(str, row)) + "\n" for row in rows)
)
print(f"{len(rows)} classes; report totals written to {output}")
