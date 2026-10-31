# 038: full reactor on the main tree (batch 2 and batch 3 without D24/D25)

Date: 2026-10-27. `rtk mvn -o -Dlicense.skipUpdateLicense=true clean test`, Maven 3.9.16,
Oracle GraalVM 25.0.4. Tree: batch 2 plus D20 (d), D21 with the interner SPI as reviewed
(RSO), D22 with review RD22T's P3s (superclass hash first, status equality in the test),
D23 with review RD23S's fix (`Apply` keeps its varargs call).

BUILD SUCCESS: xacml-core 6,646 (2 skipped), xacml-xml-jaxb 253, xacml-xml 24 (1),
xacml-pdp 217 (3), xacml-json 99, conformance 938 (12), xacml-jmh 195; 8,372 tests,
0 failures, 0 errors, 18 skipped.
