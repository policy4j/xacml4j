# Run 041: donor pre-audit for task 5.1, identifier package (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High) per the pane; the report's own label says
gemini-2.5-pro (known label drift). Session `policy4j--g1`, pane 3.
Read-only: donor `b9f114a3` via `git show`, worktree callers via `rtk rg`.
Raw report: [agy-report.txt](agy-report.txt) (A 27 defects, B behavior
changes and callers at risk, C 38 test cases).

Verdict: do not apply the donor identifier hunks as they are.

Headline defects (A):
1. `identifier/Identifier.java:103-159`: UriId, UrnId, StringId and
   StringIdCaseInsensitive define equals as "same id or any alias in
   common" and keep the record's default hashCode (id and aliases): equal
   objects can hash differently, and equality is not transitive.
2. `identifier/Issuer.java:51-54`: `match` is inverted: an "any" designator
   issuer matches only attributes without an issuer, and a specific issuer
   also matches attributes without one.
3. `UrnIdentifier.java:110`: `getById` indexes by the short id, so parsing a
   full URN does not return the constant (CategoryId, StatusCodeId).
4. `CategoryId.java:87-91`: `isDelegated()` inverted.
5. `identifier/ValueTypeId.java:27`, `CategoryId.java:129`: depend on the 5.2
   `UriVal` type, so 5.1 does not compile alone.
Also: case-insensitive Issuer (XACML compares issuers as strings),
case-insensitive UrnIdentifier equals with a hash on the short id only,
URN8141 regex rejects lowercase percent-encoding hex, URI.normalize and
java.net.URI equality in `identifier/URI.java`, JUnit 4 in donor tests.

Callers at risk (B): RequestContext category lookups, DefaultPolicyDecisionPoint
and DefaultResolverRegistry maps keyed by CategoryId, the multiple-resource
handler, StatusCode comparisons, AttributeIndex issuer matching.

## Conductor spot checks (2026-10-02)

Confirmed by reading the donor source: finding 1 (alias-intersection equals
in all four records, no hashCode override), finding 2 (`match` returns
`issuer.isAny()` when this is any, and `issuer.isAny() || equals` otherwise),
and the case-insensitive Issuer (`Issuer.issuer` builds a
StringIdCaseInsensitive). The other findings are unverified until the 5.1
packet's tests exercise them.

Use: the 5.1 packet re-implements the identifier package to design D6
(canonical-only equality and hashing; aliases only in registry indexes) and
uses Table C as the minimum test list; donor code is a reference, not a
patch to apply.
