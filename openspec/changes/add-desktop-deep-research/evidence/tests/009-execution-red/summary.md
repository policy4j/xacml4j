# Execution tests: red

31 named execution-spec tests ran through the production loader. All 31 failed with NOT_IMPLEMENTED because the production script is absent. Zero passed, zero skipped. The desktop boundary is synthetic; storage, locks and command entrypoints will be real once implemented. No app was accessed.

Tests include crash-after-Send recovery, caller/trust checks, partial observations, positive unsent recovery, stale pending interactions, complete source verification before input, and persistent cross-checkout transaction locks. See receipt.json for exact named mapping and test-file hash; output.txt retains complete output with the user home normalized.
