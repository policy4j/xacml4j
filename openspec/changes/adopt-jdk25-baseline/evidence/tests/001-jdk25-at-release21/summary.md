# 001: reactor on GraalVM 25 at release 21 (task 1.2, characterization)

Date: 2026-10-27. Oracle GraalVM 25.0.4+7.1, Maven 3.9.16 offline, the tree at
`<release>21</release>`. Raw log in the scratch directory (local paths).

Result: BUILD SUCCESS. 8,202 tests, 0 failures, 0 errors, 18 skipped, the
same per module as the JDK 21 gate (add-pdp-jmh-benchmarks run 016):
xacml-core 6,501/2, xacml-xml 253/0, xacml-json 24/1, xacml-pdp 202/3,
xacml-test 99/0, xacml-conformance-tests 937/12, xacml-jmh 186/0.

JDK warnings in the log (one kind): "sun.misc.Unsafe::objectFieldOffset has
been called by com.codahale.metrics.Striped64" (Yammer metrics-core 3.0.2)
and "will be removed in a future release". Change `upgrade-dependencies`
replaces that library (Dropwizard 4.2.40). No other JDK warning.
`git status` after the build: no file changed by the build.
