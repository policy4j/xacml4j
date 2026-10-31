# policy4j-the.3.8: compare XACML 3.0 associated advice

`Xacml30TestUtility.assertResults` now compares `AssociatedAdvice`. Absent and empty advice normalize to empty; advice is compared as an unordered set by AdviceId, then assignments by AttributeId. Assignment comparison checks AttributeId, Category, Issuer, DataType, and content/value. The existing assignment comparator used by obligations now checks Category and Issuer as well.

The XACML 3.0 Core specification says `<AssociatedAdvice>` contains a set of `<Advice>` elements (section 5.33), and section 7.18 describes the advice set returned to the PEP. Thus order is not significant. See the [official XACML 3.0 Core specification](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html).

`Xacml30TestUtilityTest` covers missing and extra advice, differing AdviceId, each requested assignment difference (attribute id, category, issuer, data type, and value), order independence, absent/empty normalization, and the `assertResults` integration path. The test-first red run reports 20 tests, 4 failures, and 0 errors: only missing/extra advice, differing AdviceId, and the aggregated assignment-difference test failed. After implementation the focused class reports 20 tests, 0 failures, and 0 errors.


## rv028 follow-up: multiset comparison

rv028 identified that the earlier implementation still keyed assignments by
`AttributeId`. That loses all but the last assignment when an advice or
obligation contains repeated IDs, including the three repeated `assignment2`
values in `IIIA301Response.xml`. The implementation now uses a shared
occurrence-preserving multiset matcher for assignments and for the enclosing
advice and obligation lists. Assignment equivalence checks `AttributeId`,
`Category`, `Issuer`, `DataType`, and content. `assertAdvice` is now public,
matching `assertObligation` visibility.

Duplicate `AdviceId` and `ObligationId` entries are compared as a multiset,
including multiplicity and each entry's assignments. XACML 3.0 section 7.18
describes which obligations and advice are combined and returned through
evaluation; silently reducing the returned sequence to a map keyed by ID
would lose output. Multiset comparison keeps each returned item observable,
allows order changes, and fails if a duplicate or one of its assignments is
missing or changed. See the [OASIS XACML 3.0 Core specification, section
7.18](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html).

Four new tests were run red first. The 24-test focused class had four failures:
permuted same-ID assignments in advice and obligations, and duplicate advice
and obligation IDs whose earlier entry was changed. The existing ID-keyed
maps accepted the changed duplicate entries and rejected valid permutations.
After the change all 24 tests passed.

The JDK 21 conformance reactor passed with no failing cases: OASIS 2.0 347,
OASIS 3.0 347, RSA 2008 17, and examples 2. Since no case failed, no
harness/engine/fixture classification was required. Surefire reports show
`xacml-test` 30 tests and `xacml-conformance-tests` 42 test methods, all with
zero failures, errors, or skips. Raw rv028 logs are `red-rv028.log`,
`green-rv028-focused.log`, and `conformance-rv028.log`; `summarize_conformance.py`
recounts the executed cases and Surefire totals. The same helper verifies that
both edited Java license headers are byte-identical to the `.base` lane copy.
