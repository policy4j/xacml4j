# Conductor validation

The terminal baseline review was inspected before promotion. Its central coverage finding is supported by the unchanged main source: `Rule.EngineOnly.expression` admits exact designators/selectors without proving their runtime elements, and the existing `RulesInPolicyContextPdpTest` placements use either a custom function/predicate or a custom literal. Those placements cover fallback behavior rather than the admitted runtime-value path. The report calls this scanner `Rule.Eligibility`; the actual nested class is `Rule.EngineOnly`. The path and behavioral finding are correct.

The required correction coverage is request- and PIP-supplied custom values under an otherwise built-in higher-order function, compared with ordinary observed evaluation and both target-index modes. Status/response preservation, lazy resolution, engine-literal controls and D45 interaction are useful checks to retain or extend where existing tests do not already cover the outcome. A target-path test is appropriate only if that expression shape is representable; the existing custom target callback must not be mislabeled as higher-order runtime-value coverage.

This completes the bounded baseline coverage-review packet `policy4j-cwtu.42.14.2`. It does not accept D47, does not close parent task 42.14, and reports no executed test result. The candidate requires a separate review after its tests and source hashes are available.

The terminal report, summary and command record are retained with source/retained hashes in `provenance.json`. Machine-local paths in promoted text are represented by `${PROJECT_ROOT}` or `${USER_HOME}`.
