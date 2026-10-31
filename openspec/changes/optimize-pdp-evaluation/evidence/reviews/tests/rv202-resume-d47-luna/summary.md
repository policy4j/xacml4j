# Summary

Baseline D47 test review: **coverage gap found; candidate not reviewed or accepted.**

P2: no test sends a request- or PIP-supplied custom `Value` through an otherwise eligible built-in higher-order function and compares observed/unobserved results with target index off/on. Existing custom callback and custom-literal cases are conservative fallback controls, not coverage of the faulty optimized path.

The report names concrete tests for request/PIP runtime values, status and response preservation, lazy PIP resolution, engine-literal behavior, and D45 interaction. No build or test was run. Baseline: `759d90a6c75510d55127d8e22870f9edd985c991`.
