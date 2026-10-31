# Research

Status: REQUIRED. Which external test vectors exist, where they come from, under which terms they can be
vendored, and how our conformance module is wired: answered by a four-lane NTM swarm on 2026-10-27
(session `xacml4j--pdp-profile-2`, read-only packets, reports stored here with paths made
repository-relative).

## Lanes

| Lane | Model | Scope | Report |
| --- | --- | --- | --- |
| TV1 | Claude Opus 5.5 | Java engines (AT&T, OpenAZ, AuthzForce, Balana, Sun XACML, Heras-AF, PicketBox, others) | `tv1-opus-java-engines.md` |
| TV2 | Codex GPT-6.1 Terra | OASIS assets, profiles, non-Java engines, vendor examples, research datasets | `tv2-terra-profiles-other-engines.md` |
| TV3 | Claude Fable 5.1 | this repository: how the suites are wired, a streamlined design, coverage gaps | `tv3-fable-conformance-audit.md` |
| TV4 | Codex GPT-6 Astra | fixture fingerprints across public copies, license verification | `tv4-astra-fingerprints-licenses.md` |

Tool notes: the Codex lanes' GitHub connector asked for a sign-in; it was dismissed (no sign-in) and the
lanes used Tavily, DuckDuckGo, and public GitHub URLs instead (UNAVAILABLE: GitHub connector). Some OASIS
pages returned 403, 500, or 520 (recorded in TV2 and TV4).

## Answers

1. **Is there an OASIS XACML 3.0 conformance suite?** No normative one. The XACML 3.0 core (§10.2) points
   to the TC page, which lists only the 1.x and 2.0 suites (TV1, TV2). Every Java engine uses the same
   non-normative 3.0 set, AT&T's April 2014 submission; its index says "Version 0.5, January 2014" and
   calls the tests draft and non-normative (TV2).
2. **What do we hold?** `xacml3.0-ct-v.0.4`: all 1,707 files equal AT&T's copy byte for byte (TV1, TV4).
   `oasis-xacml20-compat-test`: the OASIS 2.0 v0.4 package, 1,143 files, whitespace and seven id edits,
   without the three cases of OASIS patch #1 (TV1). `rsa2008-interop`: 34 request and response files carry
   the project's LGPL header, a provenance defect (TV3).
3. **Which sources add value?**
   - AuthzForce (Apache-2.0): an errata list of about 33 defects in the 3.0 set, a mandatory (455),
     optional (18), and unsupported (14) split, 23 additional cases, JSON validation fixtures (TV1, TV4).
   - OASIS 2.0 patch #1: IIC233, IIF001, IIF002 (TV1, TV2).
   - Small licensed examples: Curity JSON (Apache-2.0), Go decision assertions (MIT), `ndg_xacml` (BSD)
     (TV2). Balana `basic/3` (56 cases) and AT&T on-permit-apply-second policies to review (TV1).
   - Copies to skip: OpenAZ, Heras-AF, enterprise-java-xacml, PicketBox, .NET, Go-translator, and Rust
     collections largely reproduce suites we hold (TV1, TV2, TV4).
4. **Licenses.** MIT, BSD, and Apache-2.0 project fixtures can be vendored with their notices (TV1, TV4).
   GPL-3.0 AuthzForce repositories (`restful-pdp`, `server`, `authzen`, `fiware`) cannot be relabelled
   LGPL; skip them (TV1, TV4). The OASIS IPR policy permits copying with notices and limited derivatives
   but is not a software license; the legacy grant for the test XML is unclear (TV4, open).
5. **Corrections versus conventions.** The AuthzForce README mixes schema repairs, harness choices,
   engine conventions (current-time override, `IIIE302` attribute order, `ReturnPolicyIdList`
   interpretation), and unsupported features; each needs its own disposition, and a convention must not
   become our conformance rule (TV4, TV1).
6. **Our harness.** Four runners and three comparators over 880 manifest rows; special cases hard-coded
   by case id; a vendored `PIP.txt`, five repository property files, two JSON responses, and 72
   deprecated 3.0 cases unused; the 3.0 comparator checks the status code only (TV3).
7. **Coverage gaps.** 26 XACML 3.0 conversion functions, `string-equal-ignore-case`,
   `string-concatenate`, `time-in-range`, non-string `*-regexp-match`, `ipAddress` and `dnsName` bag and
   set functions, `on-permit-apply-second`, variable definitions, combiner parameters, `PolicyIssuer`,
   `CombinedDecision`, JSON requests (TV3).
8. **Research datasets.** Targen and the mutation operators (Martin and Xie), X-CREATE (Bertolino et al.),
   Margrave examples, the ASER benchmark index, Prezta: useful ideas; no licensed, counted vector set
   confirmed (TV2).

## Open

- The OASIS terms for the legacy test XML (TV4).
- The standard ids of the `ipAddress` and `dnsName` set functions (TV3).
- The novelty of the AuthzForce extra cases against our unit tests (TV1, TV4).
