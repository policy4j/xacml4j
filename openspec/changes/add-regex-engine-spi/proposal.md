## Why

The six XACML `*-regexp-match` functions (`xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`)
are bound to `java.util.regex`. Each call translates the XML Schema pattern to
Java syntax and compiles it again: the per-thread cache runs only when the
system property `...RegExpFunctions.cacheRegExps` is `true` (`:178`, `:396`),
which it is not by default. A backtracking engine also forces the deadline,
input, and repetition limits of bead 4.58, and still leaves a known gap: a
pattern can make the matcher work without reading input. The maintainer asked
on 2026-10-26: "For string operations create regexp spi and implement spi for
jdk regexp and google re2 regexps". RE2/J matches in time linear in the input
and has no backtracking.

## What Changes

- A regular-expression engine SPI in `xacml-core`: an engine compiles an XACML
  (XML Schema) pattern once into a compiled form that answers "does any
  substring of the input match" (XPath F&O `fn:matches`), with the existing
  limits. Engines are found with `ServiceLoader` and selected by name.
- Engine `jdk` in `xacml-core`: today's behavior (translation, limits,
  deadline), moved behind the SPI, with compiled patterns cached across calls.
  It stays the default.
- Engine `re2j` in a new module (`xacml-regex-re2j`), on
  `com.google.re2j:re2j` 1.8: translation from XML Schema syntax to RE2
  syntax; patterns that RE2 cannot express are rejected at compile time with
  a defined status.
- How a PDP or a policy repository selects the engine, and how the compiled
  pattern of a constant first argument is reused, are design decisions.
- A JMH benchmark of the regexp functions per engine, and a regex corpus for
  `PdpDecideBenchmark` (needs `add-pdp-jmh-benchmarks`).
- **BREAKING** is possible for the system properties of `RegExpFunctions`; the
  design decides.

## Capabilities

### New Capabilities

- `regex-engine-spi`: engine selection, the compile-once contract, the
  matching semantics, the limits, and the errors of each engine.

### Modified Capabilities

None (`openspec/specs/` is empty).

## Research Questions

- Q1. Which XML Schema regular-expression features (XML Schema Part 2,
  Appendix F; XPath F&O 3.1 §5.6.1) can RE2 syntax express, and which need a
  translation or a rejection (character-class subtraction, `\i`, `\c`, Unicode
  block escapes `\p{IsX}`, `$` semantics)?
- Q2. Which XACML conformance cases use `*-regexp-match`, and do they pass
  with each engine?
- Q3. Current RE2/J release, license, JDK 25 support, and its Unicode class
  support.
- Q4. Engine selection: global property, PDP builder, or evaluation context;
  what other XACML engines do.
- Q5. Do the bead 4.58 limits still apply to RE2/J, and which status a limit
  or an unsupported pattern yields (XACML 3.0 §7.19 Indeterminate,
  processing-error versus syntax-error).

## Verification

- Characterization tests of today's `RegExpFunctions` behavior first.
- The OASIS and RSA conformance suites with the `jdk` engine (unchanged) and
  with the `re2j` engine (every regexp case passes or is listed with the
  reason).
- Engine contract tests run against both engines.
- JMH: regexp function benchmark per engine; `PdpDecideBenchmark` on a regex
  corpus, before and after.

## Impact

- `xacml-core` (`RegExpFunctions`, a new SPI package), a new module
  `xacml-regex-re2j`, the reactor POM, `xacml-jmh` (benchmarks).
- New dependency `com.google.re2j:re2j` 1.8 (Go-style BSD license), in the new
  module only. The maintainer asked for it (2026-10-26).
- Planning: a discovery swarm (Claude Opus 5.5, Claude Fable 5.1, Codex Sol 6.1,
  Astra, Terra, Agy Argon) answers the research questions and drafts the
  design options before `design.md` is written.

## Maintainer Decisions (2026-10-28)

The maintainer approved implementation and decided design Open Questions 1 to 5
and the **BREAKING** items:

1. Engine selection: per function provider and policy repository (D3).
2. Default profile: `legacy` stays the default; `xf` as the default is a later
   change of its own.
3. Statuses: the D5 matrix is adopted. A pattern malformed in its profile's
   grammar gives syntax-error with a fixed message; an engine `Error` gives
   processing-error with the fixed limit message (**BREAKING** for a client that
   reads these status codes or messages).
4. Defaults: for the `xf` profile, a count bound of 1000 and the compile-size
   cap; a compiled-pattern cache of 256 entries per engine instance. The
   `cacheRegExps` property is accepted and ignored with a warning, so compiled
   patterns are always reused (D6, **BREAKING** only for code that relied on
   recompilation).
5. `xf` grammar: strict XF 1.0 (23 January 2007), the edition XACML A.3.13
   cites; later-edition constructs (`.` excluding CR, `(?:...)`) are outside
   the profile.

## Tracking

Beads epic: `policy4j-eufz`. Task X.Y maps to issue `<epic>.X.Y`.
