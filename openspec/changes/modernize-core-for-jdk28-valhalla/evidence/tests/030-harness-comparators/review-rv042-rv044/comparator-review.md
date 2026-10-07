# rv042/rv044 comparator decisions

The source is [XACML 3.0, section 7.18](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html). Returned obligations come from paths whose effect matches the PDP result. A 3.0 response obligation has no FulfillOn field. When a domain response omits it, the comparator supplies the enclosing Permit/Deny decision; an explicit contradictory effect remains a mismatch.

Run 05 exposed two existing Xacml30SamplePolicyTest failures after a strict null-versus-effect comparison. Run 06 adds Permit and Deny tests first and records four failures across 14 tests (the two samples and the two new invocations). Run 07 is green. Neither sample test nor fixture was changed.

Collection matching copies the actual collection and removes each matched entry. It keeps the existing full-object assertion and now compares obligation FulfillOn as well as its id and assignment multiset. Result's builder merges repeated obligation ids, so the focused duplicate-collection test invokes the private production comparator reflectively. This preserves real obligation objects and avoids adding a production API solely for a test. Public-response controls still exercise the sample and conformance entrypoints.

The double oracle parses both values with their declared type and uses a == b or both NaN. Signed zeros therefore match, and NaN does not match an ordinary number. This rule applies only in Xacml30TestUtility; this lane did not edit DoubleVal or engine equality.

URI negative controls reject dot-segment normalization and scheme/host case folding in both comparison directions. The core AnyURI repair supplies the same lexical equality to the harness.

rv042's advice finding is superseded by rv044. Advice matching keeps the existing full-object equality assertion. No temporal, duration or string-ignore-case engine semantics were edited by this lane.

Run 09 attempted the two conductor-requested re-enablings with empty reasons. The manifest guard rejected the setup before any fixture case ran (six errors in 25 non-enumerated invocations). Both rows now have the standard execution reason. Run 10 retries the complete conformance gate; later final receipts supersede run 09.
