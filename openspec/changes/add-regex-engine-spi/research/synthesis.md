# Discovery synthesis (task 1.1)

Six read-only lanes ran on 2026-10-27 (lane table in `README.md`). Nothing
here was executed: every claim about engine behavior is from source reading
or cited sources, and the characterization tests of task 2.1 confirm or
refute it.

## Adopted

| Area | Finding | Lanes | Design |
| --- | --- | --- | --- |
| Semantics | XACML A.3.13 uses `fn:matches` of XF 1.0 (23 January 2007): substring search, `^` and `$` anchor the string, no flags; not the XSD `pattern` facet | RX1, RX4 | Context, D1 |
| Translation | RE2/J needs translation for `.`, `\s \d \w`, `\i \c`, block escapes, `\p{Cn}`, `\p{C}`, subtraction; five constructs are misread silently; a validating parser is needed | RX1 | D1 (`XacmlPattern`), D3 |
| Unicode | RE2/J tables are Unicode 6.0, JDK 25 is 16.0; ranges are generated from the JDK's data | RX1, RX5 | D3 |
| Limits | No DFA budget in RE2/J; nested repetition and program size unbounded; Go's product bound of 1000; the deadline still works through `CharSequence` | RX2 | D5 |
| Statuses | XACML 7.19.2 and B.8 support syntax-error for a malformed pattern; unsupported and limits stay processing-error | RX2 | D5 (Open Question 3) |
| Reuse | Compiled patterns shared, matching state per call; literal bindings at the call site, a bounded cache for the rest | RX4, RX6 (AuthzForce precedent) | D4 |
| Coverage | Conformance has 9 regexp cases per OASIS suite, simple ASCII patterns; RSA has none; a dedicated corpus is needed | RX1, RX3 | Test Strategy |
| Other engines | No other open-source XACML engine has an SPI, a linear-time engine, or ReDoS limits | RX6 | Context |
| Releases | RE2/J 1.8 latest (BSD-3, Java 6 bytecode, no runtime dependencies); issues 168 and 198 | RX5 | D6 |

## Disagreements

- Engine selection: RX2 recommends an engine per function provider and
  repository (known at policy load); RX4 recommends a frozen evaluation
  environment per PDP. Both reject selection by the request and silent
  fallback. Design D2 proposes RX2's option and lists RX4's as the
  alternative (Open Question 1).
- Performance: RX5 notes that published short-input measurements often favor
  precompiled `java.util.regex`; the value of `re2j` is the bound, not speed.
  The JMH pairs decide what to claim.

## Open for the maintainer

Design Open Questions 1 to 4: selection, today's `jdk` drifts, statuses, and
the default limits.
